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
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.RejectComplaintDto;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.exception.InvalidStatusTransitionException;
import com.visioners.civic.complaint.exception.ResourceNotFoundException;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.NotificationType;
import com.visioners.civic.complaint.notification.ComplaintNotificationService;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.exception.*;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.staff.repository.StaffRepository;
import com.visioners.civic.staff.service.StaffService;
import com.visioners.civic.util.SmsService;

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
    
    /** View complaints with optional filters */
    public Page<ComplaintViewDTO> viewDeptComplaints(UserPrincipal principal, Pageable page,
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

        return complaints.map(ComplaintService::mapToComplaintViewDTO);
    }

    /** Assign complaint to worker */
    public ComplaintViewDTO assignComplaint(UserPrincipal principal, AssignComplaintDTO dto) {
        Staff officer = staffService.getStaff(principal.getUser());
        Staff worker = staffService.getStaff(dto.getWorkerId());
        Complaint complaint = complaintService.getComplaintByComplaintId(dto.getComplaintId());

        validateAssignment(complaint, officer, worker);

        complaint.setAssignedBy(officer);
        complaint.setAssignedTo(worker);
        complaint.setAssignedAt(Instant.now());
        complaint.setStatus(IssueStatus.ASSIGNED);
        complaint.setActionedAt(Instant.now());

        complaintRepository.save(complaint);

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

        return ComplaintService.mapToComplaintViewDTO(complaint);
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

        return ComplaintService.mapToComplaintViewDTO(complaint);
    }

    /** Reject resolved complaint */
    public ComplaintViewDTO rejectComplaint(UserPrincipal principal, RejectComplaintDto dto) {
        Staff officer = staffService.getStaff(principal.getUser());
        Complaint complaint = complaintService.getComplaintByComplaintId(dto.complaintId());

        validateRejection(complaint, officer);

        complaint.setRejected(true);
        complaint.setStatus(IssueStatus.ASSIGNED);
        complaint.setActionedBy(officer);
        complaint.setRejectionNote(dto.rejectionNote());

        complaintRepository.save(complaint);

        notificationService.notifyFieldWorker(complaint.getComplaintId(), complaint.getAssignedTo().getUser().getId(), NotificationType.REJECTED_COMPLAINT);
        
        return ComplaintService.mapToComplaintViewDTO(complaint);
    }

    /** Complaint statistics for department */
    public DepartmentComplaintStatisticsDTO getStatistics(UserPrincipal principal, Date from, Date to) {
        Staff officer = staffService.getStaff(principal.getUser());

        Specification<Complaint> spec = Specification.unrestricted();
        spec
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

        return DepartmentComplaintStatisticsDTO.builder()
                .totalComplaints(total)
                .openCount(open)
                .assignedCount(assigned)
                .resolvedCount(resolved)
                .rejectedCount(rejected)
                .closedCount(closed)
                .build();
    }

     public ComplaintViewDTO getComplaintByComplaintIdDetail(UserPrincipal principal, long complaintId) {
        Staff officer = staffRepository.findByUser(principal.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        if (!complaint.getDepartment().getId().equals(officer.getDepartment().getId())) {
            throw new IllegalArgumentException("Complaint does not belong to your department");
        }

        return ComplaintService.mapToComplaintViewDTO(complaint);
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
