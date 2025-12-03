package com.visioners.civic.complaint.service;


import org.springframework.stereotype.Service;

import com.visioners.civic.community.service.CommunityInteractionService;
import com.visioners.civic.complaint.dto.ComplaintView;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.DeptComplaintsSummaryDTO;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.entity.ComplaintFeedback;
import com.visioners.civic.complaint.model.Location;
import com.visioners.civic.complaint.dto.feedback.ViewFeedbackDTO;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.exception.ComplaintNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintFeedbackService complaintFeedbackService;
    private final CommunityInteractionService communityInteractionService;

    public Complaint getComplaintByComplaintId(String complaintId){
        return complaintRepository.findByComplaintId(complaintId)
            .orElseThrow(() -> new  ComplaintNotFoundException ("no complaint exists with id: " + complaintId));
    }   

    //helper methods
    public static ComplaintView getComplaintView(Complaint complaint){
        return ComplaintView.builder()
                    .complaintId(complaint.getComplaintId())
                    .raidedBy(complaint.getRaisedBy().getMobileNumber())
                    .imageUrl(complaint.getImageUrl())
                    .assignedBy(complaint.getAssignedBy().getUser().getUsername())
                    .assignedTo(complaint.getAssignedTo().getUser().getUsername())
                    .severity(complaint.getSeverity())
                    .status(complaint.getStatus())
                    .solutionImageUrl(complaint.getSolutionImageUrl())
                    .solutionNote(complaint.getSolutionNote())
                    .location(convertToLocation(complaint))
                    .build();
    }

    public DeptComplaintsSummaryDTO mapToComplaintSummaryDTO(Complaint complaint) {
        if (complaint == null) {
            return null;
        }

        return DeptComplaintsSummaryDTO.builder()
                .complaintId(complaint.getComplaintId())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .severity(complaint.getSeverity() != null ? complaint.getSeverity() : null)
                .location(convertToLocation(complaint))
                .assignedBy(
                    complaint.getAssignedBy() != null 
                        ? complaint.getAssignedBy().getUser().getUsername()
                        : null
                )
                .assignedTo(
                    complaint.getAssignedTo() != null 
                        ? complaint.getAssignedTo().getUser().getUsername()
                        : null
                )
                .category(complaint.getCategory().name())
                .subCategory(complaint.getSubCategory().name())
                .imageUrl(complaint.getImageUrl())
                .createdAt(complaint.getCreatedAt())
                .assignedAt(complaint.getAssignedAt())
                .resolvedAt(complaint.getResolvedAt())
                .solutionImageUrl(complaint.getSolutionImageUrl())
                .commentCount(communityInteractionService.getCommentCount(complaint))
                .supportCount(communityInteractionService.getSupportCount(complaint))
                .build();
    }

    public ComplaintViewDTO mapToComplaintViewDTO(Complaint complaint) {
        if (complaint == null) {
            return null;
        }

        ComplaintFeedback  fb = complaintFeedbackService.getFeedbacks(complaint.getRaisedBy(), complaint).orElse(null);
        ViewFeedbackDTO fbDto = fb ==  null ?  null : new ViewFeedbackDTO(
            fb.getComment(),
            fb.getRating(),
            fb.getCreatedAt()
        );

        return ComplaintViewDTO.builder()
                .complaintId(complaint.getComplaintId())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .severity(complaint.getSeverity() != null ? complaint.getSeverity() : null)
                .location(convertToLocation(complaint))
                .assignedBy(
                    complaint.getAssignedBy() != null 
                        ? complaint.getAssignedBy().getUser().getUsername()
                        : null
                )
                .assignedTo(
                    complaint.getAssignedTo() != null 
                        ? complaint.getAssignedTo().getUser().getUsername()
                        : null
                )
                .imageUrl(complaint.getImageUrl())
                .createdAt(complaint.getCreatedAt())
                .assignedAt(complaint.getAssignedAt())
                .resolvedAt(complaint.getResolvedAt())
                .solutionNote(complaint.getSolutionNote())
                .solutionImageUrl(complaint.getSolutionImageUrl())
                .rejectionNote(complaint.getRejectionNote())
                .feedback(fbDto)
                .category(complaint.getCategory().name())
                .subCategory(complaint.getSubCategory().name())
                .communityDetails(communityInteractionService.getDetail(complaint))
                .build();
    }

    public static Location convertToLocation(Complaint complaint){

        return Location.builder()
                        .block(complaint.getBlock().getName())
                        .district(complaint.getDistrict().getName())
                            .department(complaint.getDepartment() == null ? null : complaint.getDepartment().getName())
                            .departmentId(complaint.getDepartment() == null ? null : complaint.getDepartment().getId())
                        .latitude(complaint.getLocationPoint().getY())
                        .longitude(complaint.getLocationPoint().getX())
                        .build();
    }
}
