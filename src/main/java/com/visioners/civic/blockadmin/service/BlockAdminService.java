package com.visioners.civic.blockadmin.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.blockadmin.dto.*;
import com.visioners.civic.complaint.entity.Block;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.entity.Department;
import com.visioners.civic.complaint.entity.ReopenComplaint;
import com.visioners.civic.complaint.model.ActionType;
import com.visioners.civic.complaint.model.ActorType;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.NotificationType;
import com.visioners.civic.complaint.model.ReopenStatus;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.complaint.service.ComplaintAuditService;
import com.visioners.civic.notification.ComplaintNotificationService;
import com.visioners.civic.reopen.repository.ReopenComplaintRepository;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.staff.service.StaffService;
import com.visioners.civic.user.entity.Users;
import com.visioners.civic.exception.*;
import com.visioners.civic.complaint.repository.DepartmentRepository;
import com.visioners.civic.complaint.Specifications.ComplaintSpecification;

import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class BlockAdminService {

    private final StaffService staffService;
    private final ComplaintRepository complaintRepository;
    private final ReopenComplaintRepository reopenComplaintRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintAuditService auditService;
    private final ComplaintNotificationService notificationService;

    // ============================================================
    // UTILITY: Get BA's block
    // ============================================================
    private Staff getBA(UserPrincipal principal) {
        return staffService.getStaff(principal.getUser());
    }

    // ============================================================
    // 1. ML UNKNOWN COMPLAINTS
    // ============================================================

    public Page<BAComplaintSummaryDTO> getMLUnknownComplaints(
            UserPrincipal principal, Pageable pageable) {

        Staff ba = getBA(principal);

        Page<Complaint> page = complaintRepository.findMLUnknownComplaints(
                ba.getBlock().getId(), pageable);

        return page.map(BAComplaintSummaryDTO::fromComplaint);
    }

    @Transactional
    public void routeMLUnknownComplaint(UserPrincipal principal, BAResolveUnknownDTO dto) {

        Staff ba = getBA(principal);

        Complaint complaint = complaintRepository.findByComplaintId(dto.getComplaintId())
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        if (!complaint.getBlock().getId().equals(ba.getBlock().getId())) {
            throw new AccessDeniedException("This complaint does not belong to your block.");
        }

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new InvalidDepartmentException("Invalid department"));

        complaint.setDepartment(dept);
        complaintRepository.save(complaint);

        // Audit
        auditService.log(
                complaint.getId(), null,
                ActionType.BA_ROUTED_ML, ActorType.BLOCK_ADMIN, ba.getId(),
                IssueStatus.OPEN.name(), IssueStatus.OPEN.name(),
                "Block Admin manually routed ML-unknown complaint",
                null, null,
                null, null
        );

        // Notify department
        notificationService.notifyDepartmentOfficer(
                complaint.getComplaintId(),
                dept.getId(),
                NotificationType.NEW_COMPLAINT);
    }

    // ============================================================
    // 2. PENDING (MANUAL ROUTING) FOR BA
    // ============================================================
    public Page<BAComplaintSummaryDTO> getPendingComplaints(UserPrincipal principal, Pageable pageable, java.util.Date from, java.util.Date to) {

        Staff ba = getBA(principal);

        org.springframework.data.jpa.domain.Specification<Complaint> spec = org.springframework.data.jpa.domain.Specification.unrestricted();
        spec = spec.and(ComplaintSpecification.hasBlock(ba.getBlock()))
                .and(ComplaintSpecification.hasStatus(com.visioners.civic.complaint.model.IssueStatus.PENDING))
                .and(ComplaintSpecification.hasDate(from, to));

        Page<Complaint> page = complaintRepository.findAll(spec, pageable);
        return page.map(BAComplaintSummaryDTO::fromComplaint);
    }

    @Transactional
    public void routePendingComplaint(UserPrincipal principal, com.visioners.civic.blockadmin.dto.BARoutePendingDTO dto) {

        Staff ba = getBA(principal);

        Complaint complaint = complaintRepository.findByComplaintId(dto.getComplaintId())
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        if (!complaint.getBlock().getId().equals(ba.getBlock().getId())) {
            throw new AccessDeniedException("This complaint does not belong to your block.");
        }

        if(complaint.getStatus()!=IssueStatus.PENDING){
                throw new InvalidAssignmentException("cannot manual route complaint without status PENDING");
        }

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new InvalidDepartmentException("Invalid department"));

        complaint.setDepartment(dept);
        complaint.setStatus(com.visioners.civic.complaint.model.IssueStatus.ASSIGNED);
        complaintRepository.save(complaint);

        // Audit
        auditService.log(
                complaint.getId(), null,
                ActionType.BA_ROUTED_ML, ActorType.BLOCK_ADMIN, ba.getId(),
                IssueStatus.PENDING.name(), IssueStatus.ASSIGNED.name(),
                dto.getNote(),
                null, null,
                null, null
        );

        // Notify department
        notificationService.notifyDepartmentOfficer(
                complaint.getComplaintId(),
                dept.getId(),
                NotificationType.NEW_COMPLAINT);
    }

    @Transactional
    public void rejectReopen(UserPrincipal principal, BARejectReopenDTO dto) {

        Staff ba = getBA(principal);

        Complaint complaint = complaintRepository.findByComplaintId(dto.getComplaintId())
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        if (!complaint.getBlock().getId().equals(ba.getBlock().getId())) {
            throw new AccessDeniedException("You do not control this complaint.");
        }

        ReopenComplaint reopen = reopenComplaintRepository
                .findTopByParentComplaintOrderByCreatedAtDesc(complaint)
                .orElseThrow(() -> new IllegalArgumentException("No reopen request found for this complaint"));

        // mark the reopen as rejected and record BA decision
        reopen.setStatus(ReopenStatus.REJECTED);
        reopen.setBaDecisionNote(dto.getNote());
        reopen.setBaDecisionBy(ba);
        reopen.setBaDecisionAt(Instant.now());
        reopenComplaintRepository.save(reopen);

        // block further reopens on parent
        complaint.setReopenBlocked(true);
        complaintRepository.save(complaint);

        // Audit (on parent)
        auditService.log(
                complaint.getId(), null,
                ActionType.BA_REJECT_REOPEN, ActorType.BLOCK_ADMIN, ba.getId(),
                complaint.getStatus().name(), ReopenStatus.REJECTED.name(),
                dto.getNote(),
                null, null,
                null, null
        );

        // Notify user about rejection
        notificationService.notifyUser(
                complaint.getComplaintId(),
                complaint.getRaisedBy().getId(),
                NotificationType.REOPEN_REJECTED);
    }

    @Transactional
    public void routeReopenComplaint(UserPrincipal principal, BARouteReopenDTO dto) {

        Staff ba = getBA(principal);

        Complaint complaint = complaintRepository.findByComplaintId(dto.getComplaintId())
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        if (!complaint.getBlock().getId().equals(ba.getBlock().getId())) {
            throw new AccessDeniedException("This complaint does not belong to your block.");
        }

        ReopenComplaint reopen = reopenComplaintRepository
                .findTopByParentComplaintOrderByCreatedAtDesc(complaint)
                .orElseThrow(() -> new IllegalArgumentException("No reopen request found for this complaint"));

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new InvalidDepartmentException("Invalid department"));

        reopen.setDepartment(dept);
        reopen.setStatus(ReopenStatus.ASSIGNED);
        reopen.setBaDecisionNote(dto.getNote());
        reopen.setBaDecisionBy(ba);
        reopen.setBaDecisionAt(Instant.now());
        reopenComplaintRepository.save(reopen);

        // Audit (on parent)
        auditService.log(
                complaint.getId(), null,
                ActionType.BA_ROUTED_REOPEN, ActorType.BLOCK_ADMIN, ba.getId(),
                complaint.getStatus().name(), ReopenStatus.ASSIGNED.name(),
                dto.getNote(),
                null, null,
                null, null
        );

        notificationService.notifyDepartmentOfficer(
                reopen.getReopenId(),
                dept.getId(),
                NotificationType.REOPENED_COMPLAINT);
    }

    public List<Map<String, Object>> getBlockDepartments(Block block){
        // Fetch departments via repository to avoid iterating the JPA-managed
        // collection inside the Block entity which can be concurrently modified.
        return departmentRepository.findByBlock(block)
                .stream()
                .map(d -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("departmentId", d.getId());
                    m.put("departmentName", d.getName());
                    return m;
                })
                .toList();
    }

    public Block getAdminBlockByUser(Users user){
        return staffService.getStaff(user).getBlock();
    }
}