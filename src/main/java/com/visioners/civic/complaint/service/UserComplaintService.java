package com.visioners.civic.complaint.service;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.aws.S3Service;
import com.visioners.civic.community.service.CommunityInteractionService;
import com.visioners.civic.complaint.Specifications.ComplaintSpecification;
import com.visioners.civic.complaint.dto.usercomplaintdtos.*;
import com.visioners.civic.complaint.entity.*;
import com.visioners.civic.exception.*;
import com.visioners.civic.notification.ComplaintNotificationService;
import com.visioners.civic.complaint.model.*;
import com.visioners.civic.complaint.repository.*;
import com.visioners.civic.user.entity.Users;
import com.visioners.civic.user.repository.UsersRepository;
import com.visioners.civic.util.ComplaintIdGenerator;

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
        private final GeometryFactory geometryFactory;
        private final ComplaintAudioRepository complaintAudioRepository;
        private final ComplaintIdGenerator complaintIdGenerator;
        private final ComplaintNotificationService notificationService;
        private final CommunityInteractionService communityInteractionService;
        
        /** Raise a new complaint */
        public ComplaintRaiseResponseDTO raiseComplaint(
                        ComplaintRaiseRequest request,
                        MultipartFile imageFile,
                        MultipartFile audioFile,
                        UserPrincipal principal) throws IOException {

                Users raisedBy = usersRepository.findByMobileNumber(principal.getUsername())
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                // Validate category & subcategory relationship
                Category category = request.category();
                SubCategory subCategory = request.subcategory();

                if (!subCategory.getCategory().equals(category)) {
                        throw new IllegalArgumentException(
                                        "Subcategory " + subCategory + " is not valid for category " + category);
                }

                // Validate and convert location
                Location location = request.location();
                Point pt;
                try {
                        pt = com.visioners.civic.util.GeoUtils.toPoint(
                                        geometryFactory,
                                        location.getLatitude(),
                                        location.getLongitude());
                } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid coordinates: " + e.getMessage());
                }

                // Validate district & block
                District district = districtRepository.findByName(location.getDistrict())
                                .orElseThrow(() -> new InvalidDistrictException("Invalid district"));

                Block block = blockRepository.findByName(location.getBlock())
                                .orElseThrow(() -> new InvalidBlockException("Invalid block"));

                // HARD-CODED DEPARTMENT FOR TESTING PURPOSES
                Department department = departmentRepository
                                .findByNameAndBlockId("Road Construction Department (RCD)", block.getId())
                                .orElseThrow(() -> new InvalidDepartmentException(
                                                "Department not found for block " + block.getName()));

                // Hard-coded severity for now
                IssueSeverity severity = IssueSeverity.MEDIUM;

                // Upload main image

                // Generate complaintId (Base62 incrementing)
                String complaintId = complaintIdGenerator.generateComplaintId(location);

                String imageUrl = s3Service.uploadFile(imageFile, raisedBy.getId());

                // Build complaint
                Complaint complaint = Complaint.builder()
                                .complaintId(complaintId)
                                .description(request.description())
                                .severity(severity)
                                .locationPoint(pt)
                                .imageUrl(imageUrl)
                                .category(category)
                                .subCategory(subCategory)
                                .status(IssueStatus.OPEN)
                                .raisedBy(raisedBy)
                                .district(district)
                                .block(block)
                                .department(department)
                                .build();

                complaint = complaintRepository.save(Objects.requireNonNull(complaint));

                // Upload audio if provided
                String audioUrl = audioFile != null ? s3Service.uploadAudio(audioFile, raisedBy.getId()) : null;

                if (audioUrl != null) {
                        ComplaintAudio complaintAudio = ComplaintAudio.builder()
                                        .complaint(complaint)
                                        .audioUrl(audioUrl)
                                        .build();

                        complaintAudioRepository.save(Objects.requireNonNull(complaintAudio));
                }

                // trigger notfication to the department
                notificationService.notifyDepartmentOfficer(complaintId, complaint.getDepartment().getId(),
                                NotificationType.NEW_COMPLAINT);

                return new ComplaintRaiseResponseDTO(
                                complaint.getComplaintId(),
                                complaint.getDepartment().getId(),
                                complaint.getDepartment().getName(),
                                complaint.getSeverity(),
                                IssueStatus.OPEN,
                                complaint.getCreatedAt());
        }

        /** Get list of complaints for user */
        public Page<ComplaintSummaryDTO> getAllComplaints(
                        UserPrincipal principal,
                        Pageable page,
                        IssueSeverity severity,
                        IssueStatus status,
                        Date from,
                        Date to) {

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
                                                .complaintId(c.getComplaintId())
                                                .status(c.getStatus())
                                                .severity(c.getSeverity())
                                                .location(ComplaintService.convertToLocation(c))
                                                .supportCount(communityInteractionService.getSupportCount(c))
                                                .commentCount(communityInteractionService.getCommentCount(c))
                                                .createdAt(c.getCreatedAt())
                                                .build());
        }

        /** Get details of a complaint owned by the user */
        public ComplaintDetailDTO getComplaintDetail(UserPrincipal principal, String complaintId) {

                Complaint complaint = complaintRepository.findByComplaintId(complaintId)
                                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

                if (!complaint.getRaisedBy().getId().equals(principal.getUser().getId())) {
                        throw new AccessDeniedException("You do not own this complaint");
                }

                return ComplaintDetailDTO.builder()
                                .complaintId(complaint.getComplaintId())
                                .description(complaint.getDescription())
                                .status(complaint.getStatus())
                                .severity(complaint.getSeverity())
                                .location(
                                        ComplaintService.convertToLocation(complaint)
                                        )
                                .imageUrl(complaint.getImageUrl())
                                .createdAt(complaint.getCreatedAt())
                                .assignedAt(complaint.getAssignedAt())
                                .resolvedAt(complaint.getResolvedAt())
                                .solutionNote(complaint.getStatus() == IssueStatus.RESOLVED
                                                ? complaint.getSolutionNote()
                                                : null)
                                .solutionImageUrl(complaint.getStatus() == IssueStatus.RESOLVED
                                                ? complaint.getSolutionImageUrl()
                                                : null)
                                .communityDetail(communityInteractionService.getDetail(complaint))
                                .build();
        }

        /** User complaint statistics */
        public ComplaintStatisticsDTO getUserStatistics(
                        UserPrincipal principal, Date from, Date to) {

                Users user = usersRepository.findByMobileNumber(principal.getUsername())
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                Specification<Complaint> spec = Specification.unrestricted();
                spec
                                .and(ComplaintSpecification.hasRaisedBy(user))
                                .and(ComplaintSpecification.hasDate(from, to));

                long total = complaintRepository.count(spec);
                long open = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.OPEN)));
                long assigned = complaintRepository
                                .count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.ASSIGNED)));
                long resolved = complaintRepository
                                .count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.RESOLVED)));
                long closed = complaintRepository
                                .count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.CLOSED)));

                return ComplaintStatisticsDTO.builder()
                                .totalComplaints(total)
                                .openCount(open)
                                .assignedCount(assigned)
                                .resolvedCount(resolved)
                                .closedCount(closed)
                                .build();
        }
}
