package com.visioners.civic.community.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.visioners.civic.community.dto.CommunityComplaintView;
import com.visioners.civic.community.dto.ComplaintCommunityInteractionDto;
import com.visioners.civic.community.repository.CommunityComplaintRepository;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.service.ComplaintService;
import com.visioners.civic.user.entity.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityComplaintService {

    private final CommunityComplaintRepository communityComplaintRepository;
    private final CommunityInteractionService communityInteractionService;
    
    public Page<CommunityComplaintView> getNearby(Users user, double lat, double lon, double radius, Pageable pageable) {
        Page<Complaint> complaints = communityComplaintRepository.findNearby(user.getId(), lat, lon, radius, pageable);

        // Map each Complaint to CommunityComplaintView using a viewer-aware
        // community details provider so the "supported" flag is correct.
        return complaints.map(c -> mapToComplaintView(c, user));
    }

    public CommunityComplaintView mapToComplaintView(Complaint complaint, Users viewer){
        if (complaint == null) return null;

        ComplaintCommunityInteractionDto communityDetails = communityInteractionService.getDetail(complaint, viewer);

        String assignedBy = complaint.getAssignedBy() != null ? complaint.getAssignedBy().getUser().getUsername() : null;
        String assignedTo = complaint.getAssignedTo() != null ? complaint.getAssignedTo().getUser().getUsername() : null;

        return new CommunityComplaintView(
                complaint.getComplaintId(),
                complaint.getDescription(),
                complaint.getStatus(),
                complaint.getSeverity(),
                complaint.getImageUrl(),
                ComplaintService.convertToLocation(complaint),
                assignedBy,
                assignedTo,
                complaint.getCreatedAt(),
                complaint.getAssignedAt(),
                complaint.getResolvedAt(),
                complaint.getSolutionImageUrl(),
                communityDetails
        );
    }
}
