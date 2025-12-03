package com.visioners.civic.reopen.service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.entity.ReopenComplaint;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.NotificationType;
import com.visioners.civic.complaint.model.ReopenStatus;
import com.visioners.civic.reopen.dto.*;
import com.visioners.civic.complaint.Specifications.ReopenSpecification;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.reopen.repository.ReopenComplaintRepository;
import com.visioners.civic.exception.*;
import com.visioners.civic.notification.ComplaintNotificationService;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.staff.service.StaffService;
import com.visioners.civic.user.entity.Users;
import com.visioners.civic.user.repository.UsersRepository;
import com.visioners.civic.aws.S3Service;
import com.visioners.civic.complaint.service.ComplaintAuditService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReopenService {

    private final ReopenComplaintRepository reopenRepo;
    private final ComplaintRepository complaintRepository;
    private final UsersRepository usersRepository;
    private final StaffService staffService;
    private final S3Service s3Service;
    private final ComplaintNotificationService notificationService;
    private final ComplaintAuditService auditService;

    // ---------- USER: create reopen ----------
    @Transactional
    public ReopenCreatedResponse createReopen(MultipartFile proofImage, ReopenRequest request, UserPrincipal principal) throws IOException {
        Users user = usersRepository.findByMobileNumber(principal.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Complaint complaint = complaintRepository.findByComplaintId(request.getComplaintId())
                .orElseThrow(() -> new com.visioners.civic.complaint.exception.ComplaintNotFoundException("Complaint not found"));

        // Ownership: only the user who raised complaint can reopen
        if (!Objects.equals(complaint.getRaisedBy().getId(), user.getId())) {
            throw new com.visioners.civic.exception.AccessDeniedException("You do not own this complaint");
        }

        // Only allow reopen when parent is CLOSED per decisions
        if (!Objects.equals(complaint.getStatus(), IssueStatus.CLOSED)) {
            throw new IllegalStateException("Complaint can only be reopened when it is CLOSED");
        }

        if (complaint.isReopenBlocked()) {
            throw new IllegalStateException("Further reopens for this complaint are blocked by Block Admin");
        }

        // race-safe: check existing active reopens (OPEN/ASSIGNED/PENDING)
        List<ReopenComplaint> existing = reopenRepo.findByParentComplaint(complaint);
        boolean hasActive = existing.stream().anyMatch(r ->
                r.getStatus() == null ||
                r.getStatus() == com.visioners.civic.complaint.model.ReopenStatus.OPEN ||
                r.getStatus() == com.visioners.civic.complaint.model.ReopenStatus.ASSIGNED ||
                r.getStatus() == com.visioners.civic.complaint.model.ReopenStatus.PENDING
        );
        if (hasActive) {
            throw new IllegalStateException("An active reopen request already exists for this complaint");
        }

        // upload proof
        String proofUrl = s3Service.uploadFile(proofImage, user.getId());

        // increment reopen count on parent
        int reopenCount = complaint.getReopenCount() + 1;
        complaint.setReopenCount(reopenCount);
        complaintRepository.save(complaint);

        // build reopen
        ReopenComplaint.ReopenComplaintBuilder rb = ReopenComplaint.builder()
                .parentComplaint(complaint)
                .raisedBy(user)
                .proofUrl(proofUrl)
                .note(request.getNote())
                .lat(request.getLat())
                .lon(request.getLon())
                .reopenNumber(reopenCount)
                .reopenId(complaint.getComplaintId() + "_R" + reopenCount)
                .createdAt(Instant.now()); // ensure not null

        ReopenComplaint reopen;
        if (reopenCount >= 2) {
            rb.status(com.visioners.civic.complaint.model.ReopenStatus.PENDING);
            rb.department(null);
            reopen = rb.build();
            reopenRepo.save(reopen);

            // Audit
            auditService.log(complaint.getId(), null, com.visioners.civic.complaint.model.ActionType.USER_REOPEN,
                    com.visioners.civic.complaint.model.ActorType.USER, user.getId(),
                    complaint.getStatus().name(), com.visioners.civic.complaint.model.IssueStatus.PENDING.name(),
                    request.getNote(), null, proofUrl, request.getLat(), request.getLon());

            // Notify Block Admin (BA) for his block
            notificationService.notifyBlockAdmin(reopen.getReopenId(), complaint.getBlock().getId(), NotificationType.REOPENED_COMPLAINT);
        } else {
            // first reopen: route to department if exists else to BA
            rb.status(com.visioners.civic.complaint.model.ReopenStatus.OPEN);
            rb.department(complaint.getDepartment());
            reopen = rb.build();
            reopenRepo.save(reopen);

            auditService.log(complaint.getId(), null, com.visioners.civic.complaint.model.ActionType.USER_REOPEN,
                    com.visioners.civic.complaint.model.ActorType.USER, user.getId(),
                    complaint.getStatus().name(), com.visioners.civic.complaint.model.IssueStatus.PENDING.name(),
                    request.getNote(), null, proofUrl, request.getLat(), request.getLon());

            if (complaint.getDepartment() != null) {
                notificationService.notifyDepartmentOfficer(reopen.getReopenId(), complaint.getDepartment().getId(), NotificationType.REOPENED_COMPLAINT);
            } else {
                notificationService.notifyBlockAdmin(reopen.getReopenId(), complaint.getBlock().getId(), NotificationType.FOR_ROUTING);
            }
        }

        String detail = reopenCount >= 2 ? "your complaint escalated to Block Admin for review" : "your reopen has been submitted";
        return new ReopenCreatedResponse(reopen.getReopenId(), detail);
    }

    // ---------- LISTING endpoints (generic) ----------
    public Page<ReopenComplaint> listReopensForBlockAdmin(Staff ba, Pageable pageable,
                                                         com.visioners.civic.complaint.model.IssueSeverity severity, Date from, Date to) {
        var spec = org.springframework.data.jpa.domain.Specification.<ReopenComplaint>unrestricted();
        spec = spec
                .and(ReopenSpecification.hasBlockId(ba.getBlock().getId()))
                .and(ReopenSpecification.hasStatus(ReopenStatus.PENDING))
                .and(ReopenSpecification.hasSeverity(severity))
                .and(ReopenSpecification.hasDateRange(from, to));
        return reopenRepo.findAll(spec, pageable);
    }

    public Page<ReopenComplaint> listReopensForDepartment(Staff officer, Pageable pageable, com.visioners.civic.complaint.model.ReopenStatus status,
                                                         com.visioners.civic.complaint.model.IssueSeverity severity, Date from, Date to) {
        var spec = org.springframework.data.jpa.domain.Specification.<ReopenComplaint>unrestricted();
        Long officerDeptId = officer.getDepartment() == null ? null : officer.getDepartment().getId();
        if (officerDeptId == null) {
            spec = spec
                .and((root, query, cb) -> cb.isNull(root.get("department")))
                .and(ReopenSpecification.hasStatus(status))
                .and(ReopenSpecification.hasSeverity(severity))
                .and(ReopenSpecification.hasDateRange(from, to));
        } else {
            spec = spec
                .and(ReopenSpecification.hasDepartmentId(officerDeptId))
                .and(ReopenSpecification.hasStatus(status))
                .and(ReopenSpecification.hasSeverity(severity))
                .and(ReopenSpecification.hasDateRange(from, to));
        }
        return reopenRepo.findAll(spec, pageable);
    }

    public Page<ReopenComplaint> listReopensForWorker(Staff worker, Pageable pageable, com.visioners.civic.complaint.model.ReopenStatus status,
                                                      com.visioners.civic.complaint.model.IssueSeverity severity, Date from, Date to) {
        var spec = org.springframework.data.jpa.domain.Specification.<ReopenComplaint>unrestricted();
        spec = spec
                .and(ReopenSpecification.hasAssignedWorkerId(worker.getId()))
                .and(ReopenSpecification.hasStatus(status))
                .and(ReopenSpecification.hasSeverity(severity))
                .and(ReopenSpecification.hasDateRange(from, to));
        return reopenRepo.findAll(spec, pageable);
    }

    public Page<ReopenComplaint> listReopensForUser(Users user, Pageable pageable, com.visioners.civic.complaint.model.ReopenStatus status,
                                                    com.visioners.civic.complaint.model.IssueSeverity severity, Date from, Date to) {
        var spec = org.springframework.data.jpa.domain.Specification.<ReopenComplaint>unrestricted()
                .and(ReopenSpecification.hasRaisedByUserId(user.getId()))
                .and(ReopenSpecification.hasStatus(status))
                .and(ReopenSpecification.hasSeverity(severity))
                .and(ReopenSpecification.hasDateRange(from, to));
        return reopenRepo.findAll(spec, pageable);
    }

    // ---------- GET detail (user, dept, ba) ----------
    public ReopenComplaint getReopenByReopenIdForUser(Users user, String reopenId) {
        ReopenComplaint r = findReopenByIdTrimmed(reopenId);
        if (!Objects.equals(r.getRaisedBy().getId(), user.getId()) && !Objects.equals(r.getParentComplaint().getRaisedBy().getId(), user.getId())) {
            throw new com.visioners.civic.exception.AccessDeniedException("You do not have access to this reopen");
        }
        return r;
    }

    public ReopenComplaint getReopenByReopenIdForDepartment(Staff officer, String reopenId) {
        ReopenComplaint r = findReopenByIdTrimmed(reopenId);
        Long reopenDeptId = r.getDepartment() == null ? (r.getParentComplaint().getDepartment() == null ? null : r.getParentComplaint().getDepartment().getId()) : r.getDepartment().getId();
        Long officerDeptIdLocal = officer.getDepartment() == null ? null : officer.getDepartment().getId();
        if (reopenDeptId == null || officerDeptIdLocal == null || !reopenDeptId.equals(officerDeptIdLocal)) {
            throw new IllegalArgumentException("Reopen complaint does not belong to your department");
        }
        return r;
    }

    // helper: validate, trim and fetch reopen by public id
    private ReopenComplaint findReopenByIdTrimmed(String reopenId) {
        if (reopenId == null || reopenId.isBlank()) {
            throw new com.visioners.civic.complaint.exception.ComplaintNotFoundException("Reopen id is required");
        }
        String id = reopenId.trim();
        return reopenRepo.findByReopenId(id)
                .orElseThrow(() -> new com.visioners.civic.complaint.exception.ComplaintNotFoundException("Reopen not found: " + id));
    }

    public ReopenComplaint getReopenByReopenIdForBlockAdmin(Staff ba, String reopenId) {
        ReopenComplaint r = findReopenByIdTrimmed(reopenId);
        Long blockId = r.getParentComplaint().getBlock().getId();
        if (!Objects.equals(blockId, ba.getBlock().getId())) {
            throw new IllegalArgumentException("Reopen complaint does not belong to your block");
        }
        return r;
    }

    public List<ReopenComplaint> getAllForParent(Complaint complaint) {
        return reopenRepo.findByParentComplaint(complaint);
    }


    // ---------- BA actions: reject or route to department ----------
    @Transactional
    public void baRejectReopen(Staff ba, BARejectReopenDTO dto) {
        Complaint complaint = complaintRepository.findByComplaintId(dto.getComplaintId()).orElseThrow(
                () -> new com.visioners.civic.complaint.exception.ComplaintNotFoundException("Complaint not found"));

        if (!Objects.equals(complaint.getBlock().getId(), ba.getBlock().getId())) {
            throw new com.visioners.civic.exception.AccessDeniedException("Complaint not in your block");
        }

        ReopenComplaint reopen = reopenRepo.findTopByParentComplaintOrderByCreatedAtDesc(complaint)
                .orElseThrow(() -> new IllegalArgumentException("No reopen request found for this complaint"));

        reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.REJECTED);
        reopen.setBaDecisionNote(dto.getNote());
        reopen.setBaDecisionBy(ba);
        reopen.setBaDecisionAt(Instant.now());
        reopenRepo.save(reopen);

        complaint.setReopenBlocked(true);
        complaintRepository.save(complaint);

        auditService.log(complaint.getId(), null, com.visioners.civic.complaint.model.ActionType.BA_REJECT_REOPEN,
                com.visioners.civic.complaint.model.ActorType.BLOCK_ADMIN, ba.getId(),
                complaint.getStatus().name(), com.visioners.civic.complaint.model.ReopenStatus.REJECTED.name(),
                dto.getNote(), null, null, null, null);

        notificationService.notifyUser(complaint.getComplaintId(), complaint.getRaisedBy().getId(), NotificationType.REOPEN_REJECTED);
    }

    @Transactional
    public void baRouteReopen(Staff ba, BARouteReopenDTO dto) {
        Complaint complaint = complaintRepository.findByComplaintId(dto.getComplaintId()).orElseThrow(
                () -> new com.visioners.civic.complaint.exception.ComplaintNotFoundException("Complaint not found"));

        if (!Objects.equals(complaint.getBlock().getId(), ba.getBlock().getId())) {
            throw new com.visioners.civic.exception.AccessDeniedException("Complaint not in your block");
        }

        ReopenComplaint reopen = findReopenByIdTrimmed(dto.getReopenId());

        var dept = complaint.getDepartment();
        // Use DTO departmentId (BA chooses)
        com.visioners.civic.complaint.entity.Department department = null;
        if (dto.getDepartmentId() != null) {
            department = complaint.getDepartment(); // you should fetch departmentRepository.findById(dto.getDepartmentId()) — replace accordingly
            // to keep dependency minimal, we assume dept exists; if not, throw invalid dept
        }

        // assign reopen to department and mark assigned
        reopen.setDepartment(department);
        reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.ASSIGNED);
        reopen.setBaDecisionBy(ba);
        reopen.setBaDecisionAt(Instant.now());
        reopen.setBaDecisionNote(dto.getNote());
        reopenRepo.save(reopen);

        // mark parent complaint assigned (so dept UI picks up)
        complaint.setStatus(IssueStatus.ASSIGNED);
        complaintRepository.save(complaint);

        auditService.log(complaint.getId(), null, com.visioners.civic.complaint.model.ActionType.BA_ROUTED_REOPEN,
                com.visioners.civic.complaint.model.ActorType.BLOCK_ADMIN, ba.getId(),
                IssueStatus.OPEN.name(), IssueStatus.ASSIGNED.name(),
                dto.getNote(), null, null, null, null);

        // notify department officers about reopen (pass reopenId)
        if (department != null) {
            notificationService.notifyDepartmentOfficer(reopen.getReopenId(), department.getId(), NotificationType.REOPENED_COMPLAINT);
        } else {
            // fallback: notify block admin group if dept unknown
            notificationService.notifyBlockAdmin(reopen.getReopenId(), complaint.getBlock().getId(), NotificationType.REOPENED_COMPLAINT);
        }
    }

    // ---------- Department actions ----------
    @Transactional
    public void deptAssignReopen(Staff officer, DeptAssignReopenDTO dto) {
        ReopenComplaint reopen = findReopenByIdTrimmed(dto.getReopenId());

        Long reopenDeptId = reopen.getDepartment() == null ? (reopen.getParentComplaint().getDepartment() == null ? null : reopen.getParentComplaint().getDepartment().getId()) : reopen.getDepartment().getId();
        Long officerDeptIdLocal = officer.getDepartment() == null ? null : officer.getDepartment().getId();
        if (reopenDeptId == null || officerDeptIdLocal == null || !reopenDeptId.equals(officerDeptIdLocal)) {
            throw new IllegalArgumentException("Reopen complaint does not belong to your department");
        }

        Staff worker = staffService.getStaff(dto.getWorkerId());
        Complaint parent = reopen.getParentComplaint();

        // assign parent complaint to worker
        parent.setAssignedBy(officer);
        parent.setAssignedTo(worker);
        parent.setAssignedAt(Instant.now());
        parent.setStatus(IssueStatus.ASSIGNED);
        parent.setActionedAt(Instant.now());
        complaintRepository.save(parent);

        // update reopen
        reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.ASSIGNED);
        reopenRepo.save(reopen);

        // notifications
        notificationService.notifyUser(parent.getComplaintId(), parent.getRaisedBy().getId(), NotificationType.ASSIGNED_COMPLAINT);
        notificationService.notifyFieldWorker(parent.getComplaintId(), worker.getUser().getId(), NotificationType.ASSIGNED_COMPLAINT);

        auditService.log(parent.getId(), null, com.visioners.civic.complaint.model.ActionType.ASSIGNED,
                com.visioners.civic.complaint.model.ActorType.OFFICER, officer.getUser().getId(),
                IssueStatus.OPEN.name(), parent.getStatus().name(),
                "Assigned due to reopen: " + reopen.getReopenId(), null, null, parent.getLocationPoint().getY(), parent.getLocationPoint().getX());
    }

    @Transactional
    public void deptRejectReopen(Staff officer, DeptRejectReopenDTO dto) {
        ReopenComplaint reopen = findReopenByIdTrimmed(dto.getReopenId());

        Long reopenDeptId = reopen.getDepartment() == null ? (reopen.getParentComplaint().getDepartment() == null ? null : reopen.getParentComplaint().getDepartment().getId()) : reopen.getDepartment().getId();
        Long officerDeptIdLocal = officer.getDepartment() == null ? null : officer.getDepartment().getId();
        if (reopenDeptId == null || officerDeptIdLocal == null || !reopenDeptId.equals(officerDeptIdLocal)) {
            throw new IllegalArgumentException("Reopen complaint does not belong to your department");
        }

        reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.ASSIGNED);
        reopen.setBaDecisionBy(officer); // storing officer as decision maker
        reopen.setBaDecisionAt(Instant.now());
        reopen.setBaDecisionNote(dto.getNote());
        reopenRepo.save(reopen);

        // optionally block further reopens? Business decision: here we do NOT block globally; BA can decide.
        notificationService.notifyUser(reopen.getParentComplaint().getComplaintId(), reopen.getParentComplaint().getRaisedBy().getId(), NotificationType.REOPEN_REJECTED);

        auditService.log(reopen.getParentComplaint().getId(), null, com.visioners.civic.complaint.model.ActionType.OFFICER_REJECTED,
                com.visioners.civic.complaint.model.ActorType.OFFICER, officer.getUser().getId(),
                reopen.getParentComplaint().getStatus().name(), com.visioners.civic.complaint.model.ReopenStatus.REJECTED.name(),
                dto.getNote(), null, null, null, null);
    }

    @Transactional
    public void deptApproveReopen(Staff officer, DeptApproveReopenDTO dto) {
        ReopenComplaint reopen = findReopenByIdTrimmed(dto.getReopenId());
        
        Long reopenDeptId = reopen.getDepartment() == null ? (reopen.getParentComplaint().getDepartment() == null ? null : reopen.getParentComplaint().getDepartment().getId()) : reopen.getDepartment().getId();
        
        Long officerDeptIdLocal = officer.getDepartment() == null ? null : officer.getDepartment().getId();

        if (reopenDeptId == null || officerDeptIdLocal == null || !reopenDeptId.equals(officerDeptIdLocal)) {
            throw new IllegalArgumentException("Reopen complaint does not belong to your department");
        }

        Complaint parent = reopen.getParentComplaint();

        // Approve: close parent complaint
        parent.setRejected(false);
        parent.setStatus(IssueStatus.CLOSED);
        parent.setActionedBy(officer);
        complaintRepository.save(parent);

        reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.CLOSED);
        reopen.setBaDecisionBy(officer);
        reopen.setBaDecisionAt(Instant.now());
        reopenRepo.save(reopen);

        notificationService.notifyUser(parent.getComplaintId(), parent.getRaisedBy().getId(), NotificationType.APPROVED_COMPLAINT);
        if (parent.getAssignedTo() != null) {
            notificationService.notifyFieldWorker(parent.getComplaintId(), parent.getAssignedTo().getUser().getId(), NotificationType.APPROVED_COMPLAINT);
        }

        auditService.log(parent.getId(), null, com.visioners.civic.complaint.model.ActionType.OFFICER_APPROVED,
                com.visioners.civic.complaint.model.ActorType.OFFICER, officer.getUser().getId(),
                IssueStatus.RESOLVED.name(), parent.getStatus().name(), "Approved reopen: " + reopen.getReopenId(),
                null, null, null, null);
    }

    // ---------- Worker resolves a reopen (resolves parent complaint) ----------
    @Transactional
    public void workerResolveReopen(Staff worker, MultipartFile imageFile, WorkerResolveDTO dto) throws IOException {
        
        Complaint complaint = complaintRepository.findByComplaintId(dto.getParentComplaintId())
                .orElseThrow(() -> new com.visioners.civic.complaint.exception.ComplaintNotFoundException("Complaint not found"));
        
        ReopenComplaint reopenc = findReopenByIdTrimmed(dto.getReopenId());

        if (!Objects.equals(worker.getId(), complaint.getAssignedTo() != null ? complaint.getAssignedTo().getId() : null)) {
            throw new com.visioners.civic.exception.UnauthorizedActionException("Complaint not assigned to this worker");
        }
        if (!Objects.equals(reopenc.getStatus(), ReopenStatus.ASSIGNED)) {
            throw new com.visioners.civic.complaint.exception.InvalidStatusTransitionException("Complaint is not in ASSIGNED status");
        }

        // ensure worker within radius check done externally (or reuse complaintRepository.isWorkerWithinDistance)
        String solutionImageUrl = s3Service.uploadFile(imageFile, complaint.getRaisedBy().getId());

        complaint.setResolvedAt(Instant.now());
        complaint.setSolutionImageUrl(solutionImageUrl);
        complaint.setSolutionNote(dto.getSolutionNote());
        complaint.setStatus(IssueStatus.RESOLVED);
        complaintRepository.save(complaint);

        // update reopen if present
        if (dto.getReopenId() != null && !dto.getReopenId().isBlank()) {
                ReopenComplaint reopen = findReopenByIdTrimmed(dto.getReopenId());
            reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.RESOLVED);
            reopenRepo.save(reopen);
        }

        notificationService.notifyDepartmentOfficer(complaint.getComplaintId(), complaint.getAssignedBy().getUser().getId(), NotificationType.RESOLVED_COMPLAINT);

        auditService.log(complaint.getId(), null, com.visioners.civic.complaint.model.ActionType.WORKER_RESOLVED,
                com.visioners.civic.complaint.model.ActorType.WORKER, worker.getId(),
                IssueStatus.ASSIGNED.name(), complaint.getStatus().name(), dto.getSolutionNote(),
                complaint.getSolutionImageUrl(), null, dto.getLat(), dto.getLon());
    }

    public ReopenComplaint getReopenByReopenIdForWorker(Staff worker, String reopenId) {

        ReopenComplaint r = findReopenByIdTrimmed(reopenId);

        // Worker has access only if assigned to the parent complaint
        Long assignedWorkerId = r.getParentComplaint().getAssignedTo() != null
                ? r.getParentComplaint().getAssignedTo().getId()
                : null;

        if (assignedWorkerId == null || !assignedWorkerId.equals(worker.getId())) {
            throw new com.visioners.civic.exception.AccessDeniedException(
                    "You do not have access to this reopen complaint"
            );
        }

        return r;
    }

}
