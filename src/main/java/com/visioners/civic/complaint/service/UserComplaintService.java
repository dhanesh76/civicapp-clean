package com.visioners.civic.complaint.service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.aws.S3Service;
import com.visioners.civic.community.service.CommunityInteractionService;
import com.visioners.civic.complaint.Specifications.ComplaintSpecification;
import com.visioners.civic.complaint.dto.usercomplaintdtos.*;
import com.visioners.civic.complaint.entity.*;
import com.visioners.civic.complaint.model.*;
import com.visioners.civic.complaint.repository.*;
import com.visioners.civic.exception.*;
import com.visioners.civic.notification.ComplaintNotificationService;
import com.visioners.civic.user.entity.Users;
import com.visioners.civic.user.repository.UsersRepository;
import com.visioners.civic.util.ComplaintIdGenerator;
import com.visioners.civic.ml.MLService;

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
    private final ComplaintAudioRepository audioRepository;
    private final ComplaintIdGenerator complaintIdGenerator;
    private final ComplaintNotificationService notificationService;
    private final CommunityInteractionService communityInteractionService;
    private final ComplaintAuditService auditService;
    private final MLService mlService;

    @Transactional
    public ComplaintRaiseResponseDTO raiseComplaint(
            ComplaintRaiseRequest request,
            MultipartFile imageFile,
            MultipartFile audioFile,
            UserPrincipal principal) throws IOException {

        Users user = usersRepository.findByMobileNumber(principal.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        validateCategory(request.category(), request.subcategory());
        validateMLImage(imageFile);

        Location loc = request.location();
        Point point = createPoint(loc.getLatitude(), loc.getLongitude());

        District district = districtRepository.findByName(loc.getDistrict())
                .orElseThrow(() -> new InvalidDistrictException("Invalid district"));

        Block block = blockRepository.findByName(loc.getBlock())
                .orElseThrow(() -> new InvalidBlockException("Invalid block"));

        String departmentName = mlService.routeDepartment(request.description());

        Department department;
        boolean mlUnknown = false;
        IssueStatus status;

        if (departmentName.equalsIgnoreCase("UNKNOWN")) {
            // ML could not classify → BA must route it later
            department = null;
            mlUnknown = true;
            status = IssueStatus.PENDING;
        } else {
            status = IssueStatus.OPEN;
            department = departmentRepository
                    .findByNameAndBlockId(departmentName, block.getId())
                    .orElseThrow(() -> new InvalidDepartmentException("Invalid department for block"));
        }

        String complaintId = complaintIdGenerator.generateComplaintId(loc);
        String imageUrl = s3Service.uploadFile(imageFile, user.getId());

        Complaint complaint = Complaint.builder()
                .complaintId(complaintId)
                .description(request.description())
                .severity(IssueSeverity.MEDIUM) // Later upgraded by ML
                .locationPoint(point)
                .imageUrl(imageUrl)
                .category(request.category())
                .subCategory(request.subcategory())
                .status(status)
                .raisedBy(user)
                .district(district)
                .block(block)
                .department(department)
                .build();

        complaint = complaintRepository.save(complaint);

        // Optional audio
        handleAudioUpload(audioFile, complaint, user);

        // Notify if ML classified dept
        if (!mlUnknown) {
            notificationService.notifyDepartmentOfficer(
                    complaintId,
                    department.getId(),
                    NotificationType.NEW_COMPLAINT
            );
        } else {
            notificationService.notifyBlockAdmin(
                    complaintId,
                    complaint.getBlock().getId(),
                    NotificationType.FOR_ROUTING);
        }

        // Audit: CREATED
        auditService.log(
                complaint.getId(),
                null,
                ActionType.CREATED,
                ActorType.USER,
                user.getId(),
                null,
                complaint.getStatus().name(),
                "",
                null,
                null,
                loc.getLatitude(),
                loc.getLongitude()
        );

        return new ComplaintRaiseResponseDTO(
                complaint.getComplaintId(),
                mlUnknown ? -1 : (complaint.getDepartment() == null ? -1 : complaint.getDepartment().getId()),
                mlUnknown ? "TBD" : (complaint.getDepartment() == null ? "TBD" : complaint.getDepartment().getName()),
                complaint.getSeverity(),
                complaint.getStatus(),
                complaint.getCreatedAt()
        );
    }

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

        spec  = spec.and(ComplaintSpecification.hasRaisedBy(user))
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

    // ---------------------------------------------------------------------------
    //                    GET SINGLE COMPLAINT DETAIL (USER)
    // ---------------------------------------------------------------------------
    public ComplaintDetailDTO getComplaintDetail(UserPrincipal principal, String complaintId) {

        Complaint complaint = complaintRepository.findByComplaintId(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        if (!complaint.getRaisedBy().getId().equals(principal.getUser().getId())) {
            throw new AccessDeniedException("You do not own this complaint");
        }

        Optional<ComplaintAudio> audioOpt = audioRepository.findByComplaintId(complaint.getId());

        return ComplaintDetailDTO.builder()
                .complaintId(complaint.getComplaintId())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .severity(complaint.getSeverity())
                .audioUrl(audioOpt.map(ComplaintAudio::getAudioUrl).orElse(null))
                .location(ComplaintService.convertToLocation(complaint))
                .imageUrl(complaint.getImageUrl())
                .createdAt(complaint.getCreatedAt())
                .assignedAt(complaint.getAssignedAt())
                .resolvedAt(complaint.getResolvedAt())
                .solutionNote(
                        complaint.getStatus() == IssueStatus.RESOLVED
                                ? complaint.getSolutionNote() : null)
                .solutionImageUrl(
                        complaint.getStatus() == IssueStatus.RESOLVED
                                ? complaint.getSolutionImageUrl() : null)
                .communityDetail(communityInteractionService.getDetail(complaint))
                .build();
    }

    // ---------------------------------------------------------------------------
    //                   STATISTICS FOR USER DASHBOARD
    // ---------------------------------------------------------------------------
    public ComplaintStatisticsDTO getUserStatistics(
            UserPrincipal principal, Date from, Date to) {

        Users user = usersRepository.findByMobileNumber(principal.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Specification<Complaint> spec = Specification.unrestricted();
        spec=spec.and(ComplaintSpecification.hasRaisedBy(user))
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

    private void validateCategory(Category c, SubCategory s) {
        if (!s.getCategory().equals(c)) {
            throw new IllegalArgumentException("Invalid subcategory for category");
        }
    }

    private void validateMLImage(MultipartFile image) {
        if (!mlService.validateImage(image)) {
            throw new IllegalArgumentException("Uploaded image is invalid");
        }
    }

    private Point createPoint(double lat, double lon) {
        try {
            return com.visioners.civic.util.GeoUtils.toPoint(geometryFactory, lat, lon);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
    }

    private void handleAudioUpload(MultipartFile audioFile, Complaint complaint, Users user) throws IOException {
        if (audioFile == null) return;

        String audioUrl = s3Service.uploadAudio(audioFile, user.getId());
        ComplaintAudio audio = ComplaintAudio.builder()
                .complaint(complaint)
                .audioUrl(audioUrl)
                .build();

        audioRepository.save(audio);
    }
}
