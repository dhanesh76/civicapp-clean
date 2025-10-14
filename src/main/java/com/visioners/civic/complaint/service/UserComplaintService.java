package com.visioners.civic.complaint.service;

import java.io.IOException;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.aws.S3Service;
import com.visioners.civic.complaint.Specifications.ComplaintSpecification;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ComplaintDetailDTO;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ComplaintRaiseRequest;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ComplaintRaiseResponseDTO;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ComplaintStatisticsDTO;
import com.visioners.civic.complaint.dto.usercomplaintdtos.ComplaintSummaryDTO;
import com.visioners.civic.complaint.entity.Block;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.entity.Department;
import com.visioners.civic.complaint.entity.District;

import com.visioners.civic.exception.AccessDeniedException;
import com.visioners.civic.exception.ComplaintNotFoundException;
import com.visioners.civic.exception.InvalidDepartmentException;
import com.visioners.civic.exception.InvalidDistrictException;
import com.visioners.civic.exception.InvalidBlockException;
import com.visioners.civic.exception.UserNotFoundException;

import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;
import com.visioners.civic.complaint.repository.BlockRepository;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.complaint.repository.DepartmentRepository;
import com.visioners.civic.complaint.repository.DistrictRepository;
import com.visioners.civic.user.entity.Users;
import com.visioners.civic.user.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserComplaintService {

    private final ComplaintRepository complaintRepository;
    private final DistrictRepository districtRepository;
    private final BlockRepository blockRepository;
    private final DepartmentRepository departmentRepository;
    private final UsersRepository usersRepository;
    private final S3Service s3Service;

    /** Raise a new complaint */
    public ComplaintRaiseResponseDTO raiseComplaint(ComplaintRaiseRequest request, MultipartFile imageFile, UserPrincipal principal) throws IOException {

        Users raisedBy = usersRepository.findByMobileNumber(principal.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Location location = request.location();

        District district = districtRepository.findByName(location.getSubAdminArea())
                .orElseThrow(() -> new InvalidDistrictException("Invalid district"));

        Block block = blockRepository.findByName(location.getLocality())
                .orElseThrow(() -> new InvalidBlockException("Invalid block"));

        // TODO: Integrate ML server to detect department based on complaint
        Department department = departmentRepository.findByName("ROAD_DEPARTMENT")
                .orElseThrow(() -> new InvalidDepartmentException("Invalid department"));

        // TODO: Integrate ML server to assign severity dynamically
        IssueSeverity severity = IssueSeverity.MEDIUM;

        // Upload file to S3
        String imageUrl = s3Service.uploadFile(imageFile);

        // TODO: Integrate ML server to validate the image 
        Complaint complaint = Complaint.builder()
                .description(request.description())
                .raisedBy(raisedBy)
                .location(location)
                .district(district)
                .block(block)
                .department(department)
                .status(IssueStatus.OPEN)
                .imageUrl(imageUrl)
                .severity(severity)
                .build();

        complaintRepository.save(complaint);

        return new ComplaintRaiseResponseDTO(
                department.getName(),
                severity,
                IssueStatus.OPEN,
                complaint.getCreatedAt()
        );
    }

    /** Get paginated list of complaints for the logged-in user with filters */
    public Page<ComplaintSummaryDTO> getAllComplaints(UserPrincipal principal, Pageable page,
                                                      IssueSeverity severity, IssueStatus status, Date from, Date to) {

        Users user = usersRepository.findByMobileNumber(principal.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Specification<Complaint> spec = Specification.unrestricted();
                spec = spec
                .and(ComplaintSpecification.hasRaisedBy(user))
                .and(ComplaintSpecification.hasSeverity(severity))
                .and(ComplaintSpecification.hasStatus(status))
                .and(ComplaintSpecification.hasDate(from, to));

        return complaintRepository.findAll(spec, page)
                .map(c -> ComplaintSummaryDTO.builder()
                        .id(c.getId())
                        .status(c.getStatus())
                        .severity(c.getSeverity())
                        .location(c.getLocation())
                        .createdAt(c.getCreatedAt())
                        .build());
    }

    /** Get detailed view of a single complaint */
    public ComplaintDetailDTO getComplaintDetail(UserPrincipal principal, Long complaintId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        if (complaint.getRaisedBy().getId() != principal.getUser().getId()) {
            throw new AccessDeniedException("Access denied: You do not own this complaint");
        }


        return ComplaintDetailDTO.builder()
                .id(complaint.getId())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .severity(complaint.getSeverity())
                .location(complaint.getLocation())
                .imageUrl(complaint.getImageUrl())
                .createdAt(complaint.getCreatedAt())
                .assignedAt(complaint.getAssignedAt())
                .resolvedAt(complaint.getResolvedAt())
                .solutionNote(complaint.getStatus() == IssueStatus.RESOLVED ? complaint.getSolutionNote() : null)
                .solutionImageUrl(complaint.getStatus() == IssueStatus.RESOLVED ? complaint.getSolutionImageUrl() : null)
                .build();
    }

    /** Get statistics of complaints raised by the user */
    public ComplaintStatisticsDTO getUserStatistics(UserPrincipal principal, Date from, Date to) {

        Users user = usersRepository.findByMobileNumber(principal.getUsername())
                .orElseThrow(() -> new com.visioners.civic.exception.UserNotFoundException("User not found"));

        Specification<Complaint> spec = Specification.unrestricted();

               spec = spec.and(ComplaintSpecification.hasRaisedBy(user))
                .and(ComplaintSpecification.hasDate(from, to));

        long total = complaintRepository.count(spec);
        long open = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.OPEN)));
        long assigned = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.ASSIGNED)));
        long resolved = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.RESOLVED)));
        long closed = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.CLOSED)));

        return ComplaintStatisticsDTO.builder()
                .totalComplaints(total)
                .openCount(open)
                .assignedCount(assigned)
                .resolvedCount(resolved)
                .closedCount(closed)
                .build();
    }
}
