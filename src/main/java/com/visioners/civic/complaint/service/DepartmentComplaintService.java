package com.visioners.civic.complaint.service;

import java.time.Instant;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.Specifications.ComplaintSpecification;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.AssignComplaintDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentComplaintStatisticsDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.DeptComplaintsSummaryDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.RejectComplaintDto;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.entity.ReopenComplaint;
import com.visioners.civic.complaint.exception.InvalidStatusTransitionException;
import com.visioners.civic.complaint.exception.ResourceNotFoundException;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.NotificationType;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.exception.*;
import com.visioners.civic.notification.ComplaintNotificationService;
import com.visioners.civic.reopen.repository.ReopenComplaintRepository;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.staff.repository.StaffRepository;
import com.visioners.civic.staff.service.StaffService;
import com.visioners.civic.util.SmsService;
import com.visioners.civic.complaint.model.ActionType;
import com.visioners.civic.complaint.model.ActorType;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ReopenSummaryDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentComplaintService {

    private final StaffService staffService;
    private final ComplaintService complaintService;
    private final ComplaintRepository complaintRepository;
    private final StaffRepository staffRepository;
    private final SmsService smsService;
    private final ComplaintNotificationService notificationService;
    private final ComplaintFeedbackService complaintFeedbackService;
    private final ComplaintAuditService auditService;
    private final ReopenComplaintRepository reopenComplaintRepository;

    /** View complaints with optional filters */
    public Page<DeptComplaintsSummaryDTO> viewDeptComplaints(UserPrincipal principal, Pageable page,
            IssueSeverity severity, IssueStatus status, Date from, Date to) {

        Staff officer = staffService.getStaff(principal.getUser());

        Specification<Complaint> specification = Specification.unrestricted();
        specification  = specification
                .and(ComplaintSpecification.hasDepartment(officer.getDepartment()))
                .and(ComplaintSpecification.hasDistrict(officer.getDistrict()))
                .and(ComplaintSpecification.hasBlock(officer.getBlock()))
                .and(ComplaintSpecification.hasSeverity(severity))
                .and(ComplaintSpecification.hasStatus(status))
                .and(ComplaintSpecification.hasDate(from, to));

        Page<Complaint> complaints = complaintRepository.findAll(specification, page);

        return complaints.map(complaintService::mapToComplaintSummaryDTO);
    }

    /** Assign complaint to worker */
    public ComplaintViewDTO assignComplaint(UserPrincipal principal, AssignComplaintDTO dto) {
        Staff officer = staffService.getStaff(principal.getUser());
        Staff worker = staffService.getStaff(dto.getWorkerId());
        // If this assignment is for a ReopenComplaint, operate on its parent complaint
        Complaint complaint;
        ReopenComplaint reopen = null;
        
        if (dto.getReopenId() != null && !dto.getReopenId().isBlank()) {
            reopen = reopenComplaintRepository.findByReopenId(dto.getReopenId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reopen complaint not found"));

            // confirm officer department ownership
            Long reopenDeptId = reopen.getDepartment() == null ? (reopen.getParentComplaint().getDepartment() == null ? null : reopen.getParentComplaint().getDepartment().getId()) : reopen.getDepartment().getId();
            if (reopenDeptId == null || !reopenDeptId.equals(officer.getDepartment().getId())) {
                throw new IllegalArgumentException("Reopen complaint does not belong to your department");
            }

            complaint = reopen.getParentComplaint();
        } else {
            complaint = complaintService.getComplaintByComplaintId(dto.getComplaintId());
        }

        validateAssignment(complaint, officer, worker);

        complaint.setAssignedBy(officer);
        complaint.setAssignedTo(worker);
        complaint.setAssignedAt(Instant.now());
        complaint.setStatus(IssueStatus.ASSIGNED);
        complaint.setActionedAt(Instant.now());

        complaintRepository.save(complaint);

        if (reopen != null) {
            // mark reopen as assigned
            reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.ASSIGNED);
            // ensure reopen references department
            if (reopen.getDepartment() == null && complaint.getDepartment() != null) {
                reopen.setDepartment(complaint.getDepartment());
            }
            reopenComplaintRepository.save(reopen);
        }

        // notification to the user 
        smsService.sendSms(
            complaint.getRaisedBy().getMobileNumber(),
            "Dear Citizen, your complaint (" + complaint.getComplaintId() +
            ") has been assigned to our field worker " + worker.getUser().getUsername() +
            ". We appreciate your contribution toward a cleaner city."
        );

        notificationService.notifyUser(complaint.getComplaintId(), complaint.getRaisedBy().getId(), NotificationType.ASSIGNED_COMPLAINT);

        // notification to the field worker: pass the user table id (Staff.user.id)
        notificationService.notifyFieldWorker(complaint.getComplaintId(), worker.getUser().getId(), NotificationType.ASSIGNED_COMPLAINT);

        // audit: assigned
        try {
            auditService.log(
                    complaint.getId(),
                    null,
                    ActionType.ASSIGNED,
                    ActorType.OFFICER,
                    officer.getUser().getId(),
                    IssueStatus.OPEN.name(),
                    complaint.getStatus().name(),
                    "",
                    null,
                    null,
                    complaint.getLocationPoint().getY(),
                    complaint.getLocationPoint().getX());
        } catch (Exception ex) {
            // continue on audit failure
        }

        return complaintService.mapToComplaintViewDTO(complaint);
    }

    /** Assign reopen to a worker by reopenId */
    public ComplaintViewDTO assignReopen(UserPrincipal principal, String reopenId, com.visioners.civic.complaint.dto.departmentcomplaintdtos.ReopenAssignDTO dto) {
        Staff officer = staffService.getStaff(principal.getUser());

        ReopenComplaint reopen = reopenComplaintRepository.findByReopenId(reopenId)
                .orElseThrow(() -> new com.visioners.civic.complaint.exception.ResourceNotFoundException("Reopen complaint not found"));

        // department access
        Long reopenDeptId = reopen.getDepartment() == null ? (reopen.getParentComplaint().getDepartment() == null ? null : reopen.getParentComplaint().getDepartment().getId()) : reopen.getDepartment().getId();
        if (reopenDeptId == null || !reopenDeptId.equals(officer.getDepartment().getId())) {
            throw new IllegalArgumentException("Reopen complaint does not belong to your department");
        }

        Staff worker = staffService.getStaff(dto.getWorkerId());

        Complaint complaint = reopen.getParentComplaint();

        // assign parent complaint
        complaint.setAssignedBy(officer);
        complaint.setAssignedTo(worker);
        complaint.setAssignedAt(Instant.now());
        complaint.setStatus(IssueStatus.ASSIGNED);
        complaint.setActionedAt(Instant.now());

        complaintRepository.save(complaint);

        // update reopen status
        reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.ASSIGNED);
        reopenComplaintRepository.save(reopen);

        // notifications
        smsService.sendSms(
                complaint.getRaisedBy().getMobileNumber(),
                "Dear Citizen, your reopened complaint (" + complaint.getComplaintId() + ") has been assigned to our field worker " + worker.getUser().getUsername() + "."
        );

        notificationService.notifyFieldWorker(complaint.getComplaintId(), worker.getUser().getId(), NotificationType.ASSIGNED_COMPLAINT);

        try {
            auditService.log(
                    complaint.getId(),
                    null,
                    ActionType.ASSIGNED,
                    ActorType.OFFICER,
                    officer.getUser().getId(),
                    IssueStatus.OPEN.name(),
                    complaint.getStatus().name(),
                    "Assigned due to reopen: " + reopenId,
                    null,
                    null,
                    complaint.getLocationPoint().getY(),
                    complaint.getLocationPoint().getX());
        } catch (Exception ex) {
            // ignore
        }

        return complaintService.mapToComplaintViewDTO(complaint);
    }

    

    /** Reject a reopen: set reopen status to REJECTED and attach note */
    public ComplaintViewDTO rejectReopen(UserPrincipal principal, String reopenId, com.visioners.civic.complaint.dto.departmentcomplaintdtos.ReopenRejectDTO dto) {
        Staff officer = staffService.getStaff(principal.getUser());

        ReopenComplaint reopen = reopenComplaintRepository.findByReopenId(reopenId)
                .orElseThrow(() -> new com.visioners.civic.complaint.exception.ResourceNotFoundException("Reopen complaint not found"));

        Long reopenDeptId = reopen.getDepartment() == null ? (reopen.getParentComplaint().getDepartment() == null ? null : reopen.getParentComplaint().getDepartment().getId()) : reopen.getDepartment().getId();
        if (reopenDeptId == null || !reopenDeptId.equals(officer.getDepartment().getId())) {
            throw new IllegalArgumentException("Reopen complaint does not belong to your department");
        }

        reopen.setBaDecisionBy(officer);
        reopen.setBaDecisionAt(Instant.now());
        reopen.setBaDecisionNote(dto.getRejectionNote());
        reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.REJECTED);
        reopenComplaintRepository.save(reopen);

        // Optionally notify user
        notificationService.notifyUser(reopen.getParentComplaint().getComplaintId(), reopen.getParentComplaint().getRaisedBy().getId(), NotificationType.REJECTED_COMPLAINT);

        return complaintService.mapToComplaintViewDTO(reopen.getParentComplaint());
    }

    /** Approve resolved complaint */
    public ComplaintViewDTO approveComplaint(UserPrincipal principal, String complaintId) {
        Staff officer = staffService.getStaff(principal.getUser());
        Complaint complaint = complaintService.getComplaintByComplaintId(complaintId);

        validateApproval(complaint, officer);

        complaint.setRejected(false);
        complaint.setStatus(IssueStatus.CLOSED);
        complaint.setActionedBy(officer);

        complaintRepository.save(complaint);

        //notification to the user 
        smsService.sendSms(
            complaint.getRaisedBy().getMobileNumber(),
            "Your complaint (" + complaint.getComplaintId() +
            ") has been approved and officially closed. Thank you for helping improve our community!"
        );

        // notify the original user who raised the complaint (pass Users.id)
        notificationService.notifyUser(complaintId, complaint.getRaisedBy().getId(), NotificationType.APPROVED_COMPLAINT);

        // notify the field worker by their user id (Staff.user.id)
        notificationService.notifyFieldWorker(complaintId, complaint.getAssignedTo().getUser().getId(), NotificationType.APPROVED_COMPLAINT);

        // audit: officer approved
        try {
            auditService.log(
                    complaint.getId(),
                    null,
                    ActionType.OFFICER_APPROVED,
                    ActorType.OFFICER,
                    officer.getUser().getId(),
                    IssueStatus.RESOLVED.name(),
                    complaint.getStatus().name(),
                    "",
                    complaint.getSolutionImageUrl(),
                    null,
                    complaint.getLocationPoint().getY(),
                    complaint.getLocationPoint().getX());
        } catch (Exception ex) {
            // ignore
        }

        return complaintService.mapToComplaintViewDTO(complaint);
    }

    /** Approve a reopen request by reopenId */
    public ComplaintViewDTO approveReopen(UserPrincipal principal, String reopenId) {
        Staff officer = staffService.getStaff(principal.getUser());

        ReopenComplaint reopen = reopenComplaintRepository.findByReopenId(reopenId)
                .orElseThrow(() -> new ResourceNotFoundException("Reopen complaint not found"));

        // department ownership check
        Long reopenDeptId = reopen.getDepartment() == null ? (reopen.getParentComplaint().getDepartment() == null ? null : reopen.getParentComplaint().getDepartment().getId()) : reopen.getDepartment().getId();

        if (reopenDeptId == null || !reopenDeptId.equals(officer.getDepartment().getId())) {
            throw new IllegalArgumentException("Reopen complaint does not belong to your department");
        }

        // approve parent complaint
        Complaint complaint = reopen.getParentComplaint();

        // ensure approval preconditions similar to approveComplaint
        validateApproval(complaint, officer);

        complaint.setRejected(false);
        complaint.setStatus(IssueStatus.CLOSED);
        complaint.setActionedBy(officer);

        complaintRepository.save(complaint);

        // mark reopen as closed/approved
        reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.CLOSED);
        reopen.setBaDecisionBy(officer);
        reopen.setBaDecisionAt(Instant.now());
        reopenComplaintRepository.save(reopen);

        // notifications and audit similar to approveComplaint
        smsService.sendSms(
            complaint.getRaisedBy().getMobileNumber(),
            "Your complaint (" + complaint.getComplaintId() + ") has been approved and officially closed. Thank you for helping improve our community!"
        );

        notificationService.notifyUser(complaint.getComplaintId(), complaint.getRaisedBy().getId(), NotificationType.APPROVED_COMPLAINT);
        notificationService.notifyFieldWorker(complaint.getComplaintId(), complaint.getAssignedTo().getUser().getId(), NotificationType.APPROVED_COMPLAINT);

        try {
            auditService.log(
                    complaint.getId(),
                    null,
                    ActionType.OFFICER_APPROVED,
                    ActorType.OFFICER,
                    officer.getUser().getId(),
                    IssueStatus.RESOLVED.name(),
                    complaint.getStatus().name(),
                    "",
                    complaint.getSolutionImageUrl(),
                    null,
                    complaint.getLocationPoint().getY(),
                    complaint.getLocationPoint().getX());
        } catch (Exception ex) {
            // ignore
        }

        return complaintService.mapToComplaintViewDTO(complaint);
    }

    /** Reject resolved complaint */
    public ComplaintViewDTO rejectComplaint(UserPrincipal principal, RejectComplaintDto dto) {
        Staff officer = staffService.getStaff(principal.getUser());
        // If rejecting a reopen request
        if (dto.reopenId() != null && !dto.reopenId().isBlank()) {
            ReopenComplaint reopen = reopenComplaintRepository.findByReopenId(dto.reopenId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reopen complaint not found"));

            Long reopenDeptId = reopen.getDepartment() == null ? (reopen.getParentComplaint().getDepartment() == null ? null : reopen.getParentComplaint().getDepartment().getId()) : reopen.getDepartment().getId();
            if (reopenDeptId == null || !reopenDeptId.equals(officer.getDepartment().getId())) {
                throw new IllegalArgumentException("Reopen complaint does not belong to your department");
            }

            reopen.setStatus(com.visioners.civic.complaint.model.ReopenStatus.REJECTED);
            reopen.setBaDecisionBy(officer);
            reopen.setBaDecisionAt(Instant.now());
            reopen.setBaDecisionNote(dto.rejectionNote());
            reopenComplaintRepository.save(reopen);

            // notify user that reopen was rejected
            notificationService.notifyUser(reopen.getParentComplaint().getComplaintId(), reopen.getParentComplaint().getRaisedBy().getId(), NotificationType.REOPENED_COMPLAINT);

            return complaintService.mapToComplaintViewDTO(reopen.getParentComplaint());
        }

        Complaint complaint = complaintService.getComplaintByComplaintId(dto.complaintId());

        validateRejection(complaint, officer);

        complaint.setRejected(true);
        complaint.setStatus(IssueStatus.ASSIGNED);
        complaint.setActionedBy(officer);
        complaint.setRejectionNote(dto.rejectionNote());

        complaintRepository.save(complaint);

        notificationService.notifyFieldWorker(complaint.getComplaintId(), complaint.getAssignedTo().getUser().getId(), NotificationType.REJECTED_COMPLAINT);
        
        // audit: officer rejected
        try {
            auditService.log(
                    complaint.getId(),
                    null,
                    ActionType.OFFICER_REJECTED,
                    ActorType.OFFICER,
                    officer.getUser().getId(),
                    IssueStatus.RESOLVED.name(),
                    complaint.getStatus().name(),
                    complaint.getRejectionNote(),
                    null,
                    null,
                    complaint.getLocationPoint().getY(),
                    complaint.getLocationPoint().getX());
        } catch (Exception ex) {
            // ignore
        }
        return complaintService.mapToComplaintViewDTO(complaint);
    }

    /** Complaint statistics for department */
    public DepartmentComplaintStatisticsDTO getStatistics(UserPrincipal principal, Date from, Date to) {
        Staff officer = staffService.getStaff(principal.getUser());

        Specification<Complaint> spec = Specification.unrestricted();
        spec = spec
                .and(ComplaintSpecification.hasDepartment(officer.getDepartment()))
                .and(ComplaintSpecification.hasDistrict(officer.getDistrict()))
                .and(ComplaintSpecification.hasBlock(officer.getBlock()))
                .and(ComplaintSpecification.hasDate(from, to));

        long total = complaintRepository.count(spec);
        long open = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.OPEN)));
        long assigned = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.ASSIGNED)));
        long resolved = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.RESOLVED)));
        long rejected = complaintRepository.count(spec.and(ComplaintSpecification.hasRejected()));
        long closed = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.CLOSED)));
        double avgRating = complaintFeedbackService.findDepartmentAvgRating(officer.getDepartment().getId());
        
        return DepartmentComplaintStatisticsDTO.builder()
                .totalComplaints(total)
                .openCount(open)
                .assignedCount(assigned)
                .resolvedCount(resolved)
                .rejectedCount(rejected)
                .closedCount(closed)
                .avgRating(avgRating)
                .build();
    }

     public ComplaintViewDTO getComplaintByComplaintIdDetail(UserPrincipal principal, String complaintId) {
        Staff officer = staffRepository.findByUser(principal.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));

        Complaint complaint = complaintRepository.findByComplaintId(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        if (!complaint.getDepartment().getId().equals(officer.getDepartment().getId())) {
            throw new IllegalArgumentException("Complaint does not belong to your department");
        }

        return complaintService.mapToComplaintViewDTO(complaint);
    }

    /** View reopens for department with optional date filters */
    public Page<ReopenSummaryDTO> viewDeptReopens(UserPrincipal principal, Pageable page, Date from, Date to) {
        Staff officer = staffService.getStaff(principal.getUser());

        java.time.Instant fromInst = from == null ? null : from.toInstant();
        java.time.Instant toInst = to == null ? null : to.toInstant();

        // Build dynamic Specification so we only bind non-null parameters to SQL.
        org.springframework.data.jpa.domain.Specification<com.visioners.civic.complaint.entity.ReopenComplaint> spec =
                (root, query, cb) -> cb.equal(root.get("department").get("id"), officer.getDepartment().getId());

        if (fromInst != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromInst));
        }

        if (toInst != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), toInst));
        }

        Page<com.visioners.civic.complaint.entity.ReopenComplaint> reopens = reopenComplaintRepository.findAll(spec, page);

        return reopens.map(ReopenSummaryDTO::fromEntity);
    }

    /**
     * Get reopen detail by reopenId for department officers. Ensures department access.
     */
    public com.visioners.civic.complaint.dto.usercomplaintdtos.ReopenDetailDTO getReopenDetail(UserPrincipal principal, String reopenId) {
        Staff officer = staffService.getStaff(principal.getUser());

        ReopenComplaint reopen = reopenComplaintRepository.findByReopenId(reopenId)
                .orElseThrow(() -> new com.visioners.civic.complaint.exception.ResourceNotFoundException("Reopen complaint not found"));

        // Determine department ownership: reopen may have explicit department or inherit from parent complaint
        Long reopenDeptId = reopen.getDepartment() == null ? (reopen.getParentComplaint().getDepartment() == null ? null : reopen.getParentComplaint().getDepartment().getId()) : reopen.getDepartment().getId();

        if (reopenDeptId == null || !reopenDeptId.equals(officer.getDepartment().getId())) {
            throw new IllegalArgumentException("Reopen complaint does not belong to your department");
        }

        java.util.List<ReopenComplaint> all = reopenComplaintRepository.findByParentComplaint(reopen.getParentComplaint());
        java.util.List<com.visioners.civic.complaint.dto.usercomplaintdtos.ReopenSummaryDTO> previous = new java.util.ArrayList<>();
        for (ReopenComplaint r : all) {
            if (!r.getReopenId().equals(reopen.getReopenId())) {
                previous.add(com.visioners.civic.complaint.dto.usercomplaintdtos.ReopenSummaryDTO.fromEntity(r));
            }
        }

        com.visioners.civic.complaint.dto.usercomplaintdtos.ReopenSummaryDTO current = com.visioners.civic.complaint.dto.usercomplaintdtos.ReopenSummaryDTO.fromEntity(reopen);

        return com.visioners.civic.complaint.dto.usercomplaintdtos.ReopenDetailDTO.builder()
                .parentComplaintId(reopen.getParentComplaint().getComplaintId())
                .currentReopen(current)
                .previousReopens(previous)
                .build();
    }

    // ---------------- Validation Methods ----------------
    private void validateAssignment(Complaint complaint, Staff officer, Staff worker) {
        if (!complaint.getDepartment().equals(officer.getDepartment())) {
            throw new InvalidAssignmentException("Complaint does not belong to your department");
        }

        if (!worker.getDepartment().equals(officer.getDepartment())) {
            throw new InvalidAssignmentException("Worker does not belong to your department");
        }
        
        if (!complaint.getStatus().equals(IssueStatus.OPEN)) {
            throw new InvalidStatusTransitionException("Cannot assign complaint with status " + complaint.getStatus());
        }
    }

    private void validateApproval(Complaint complaint, Staff officer) {
        if (!complaint.getAssignedBy().equals(officer)) {
            throw new UnauthorizedActionException("Complaint not assigned by this officer");
        }
        if (!complaint.getStatus().equals(IssueStatus.RESOLVED)) {
            throw new InvalidStatusTransitionException("Cannot approve complaint not resolved yet");
        }
    }

    private void validateRejection(Complaint complaint, Staff officer) {
        if (!complaint.getAssignedBy().equals(officer)) {
            throw new UnauthorizedActionException("Complaint not assigned by this officer");
        }
        if (!complaint.getStatus().equals(IssueStatus.RESOLVED)) {
            throw new InvalidStatusTransitionException("Cannot reject complaint not resolved yet");
        }
    }
}
