package com.visioners.civic.complaint.service;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.aws.S3Service;
import com.visioners.civic.complaint.Specifications.ComplaintSpecification;
import com.visioners.civic.complaint.dto.ComplaintView;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintRejectView;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ResolveComplaint;
import com.visioners.civic.complaint.dto.fieldworkerdtos.FieldWorkerComplaintStatsDTO;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.NotificationType;
import com.visioners.civic.complaint.notification.ComplaintNotificationService;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.staff.service.StaffService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FieldWorkerComplaintService {

    private final StaffService staffService;
    private final ComplaintRepository complaintRepository;
    private final ComplaintService complaintService;
    private final S3Service s3Service;
    private final ComplaintNotificationService notificationService;

    public Page<ComplaintView> viewAssignedComplaints(UserPrincipal principal,
                                                      Pageable page,
                                                      IssueSeverity severity,
                                                      Date from, Date to) {

        Staff staff = staffService.getStaff(principal.getUser());

        Specification<Complaint> specification = ComplaintSpecification
                .getComplaintSpecification(severity, null, from, to)
                .and(ComplaintSpecification.hasAssignedTo(staff));

        Page<Complaint> complaints = complaintRepository.findAll(specification, page);
        return complaints.map(ComplaintService::getComplaintView);
    }

    public ComplaintView resolve(UserPrincipal principal,
                                 MultipartFile imageFile,
                                 ResolveComplaint resolveComplaintDto) throws IOException {

        Complaint complaint = complaintService.getComplaintByComplaintId(resolveComplaintDto.complaintId());
        Staff worker = staffService.getStaff(principal.getUser());
        validateResolve(worker, complaint);

        String solutionImageUrl = s3Service.uploadFile(imageFile, complaint.getRaisedBy().getId());

        complaint.setResolvedAt(Instant.now());
        complaint.setSolutionImageUrl(solutionImageUrl);
        complaint.setSolutionNote(resolveComplaintDto.solutionNote());
        complaint.setStatus(IssueStatus.RESOLVED);

        complaintRepository.save(complaint);

        notificationService.notifyDepartment(complaint.getComplaintId(), complaint.getDepartment().getId(), NotificationType.RESOLVED_COMPLAINT);

        return ComplaintService.getComplaintView(complaint);
    }

    private boolean validateResolve(Staff worker, Complaint complaint) {
        if (!worker.equals(complaint.getAssignedTo())) {
            throw new RuntimeException("Complaint " + complaint.getId() + " not assigned to staff " + worker.getId());
        }
        if (complaint.getStatus() != IssueStatus.ASSIGNED) {
            throw new RuntimeException("Complaint " + complaint.getId() + " is not in ASSIGNED status");
        }
        return true;
    }

    public Page<ComplaintRejectView> rejectedComplaints(UserPrincipal principal, Pageable page) {
        Staff worker = staffService.getStaff(principal.getUser());

        Specification<Complaint> specification = ComplaintSpecification.hasAssignedTo(worker)
                .and(ComplaintSpecification.hasRejected());

        Page<Complaint> complaints = complaintRepository.findAll(specification, page);

        return complaints.map(c -> ComplaintRejectView.builder()
                .id(c.getId())
                .raisedBy(c.getRaisedBy().getUsername())
                .assignedBy(c.getAssignedBy().getUser().getUsername())
                .assignedTo(c.getAssignedTo().getUser().getUsername())
                .imageUrl(c.getImageUrl())
                .status(c.getStatus())
                .severity(c.getSeverity())
                .location(ComplaintService.convertToLocation(c.getLocation(), c.getLocationPoint()))
                .rejectionNote(c.getRejectionNote())
                .solutionImageUrl(c.getSolutionImageUrl())
                .solutionNote(c.getSolutionNote())
                .build());
    }

    public FieldWorkerComplaintStatsDTO getComplaintStats(UserPrincipal principal, Date from, Date to) {
        Staff worker = staffService.getStaff(principal.getUser());

        Specification<Complaint> baseSpec = ComplaintSpecification
                .getComplaintSpecification(null, null, from, to)
                .and(ComplaintSpecification.hasAssignedTo(worker));

        long total = complaintRepository.count(baseSpec);
        long open = complaintRepository.count(baseSpec.and(ComplaintSpecification.hasStatus(IssueStatus.OPEN)));
        long assigned = complaintRepository.count(baseSpec.and(ComplaintSpecification.hasStatus(IssueStatus.ASSIGNED)));
        long resolved = complaintRepository.count(baseSpec.and(ComplaintSpecification.hasStatus(IssueStatus.RESOLVED)));
        long rejected = complaintRepository.count(baseSpec.and(ComplaintSpecification.hasRejected()));
        long closed = complaintRepository.count(baseSpec.and(ComplaintSpecification.hasStatus(IssueStatus.CLOSED)));

        return FieldWorkerComplaintStatsDTO.builder()
                .total(total)
                .open(open)
                .assigned(assigned)
                .resolved(resolved)
                .rejected(rejected)
                .closed(closed)
                .build();
    }

    public ComplaintViewDTO getComplaintDetail(UserPrincipal principal, String complaintId) {
        Staff worker = staffService.getStaff(principal.getUser());
        Complaint complaint = complaintService.getComplaintByComplaintId(complaintId);

        if (!worker.equals(complaint.getAssignedTo())) {
            throw new RuntimeException("You are not authorized to view this complaint");
        }

        return ComplaintService.mapToComplaintViewDTO(complaint);
    }
}
