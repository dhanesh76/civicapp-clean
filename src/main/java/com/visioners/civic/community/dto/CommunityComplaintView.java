package com.visioners.civic.community.dto;

import java.time.Instant;

import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;

import lombok.Builder;

@Builder
public  record CommunityComplaintView (
     String complaintId,
     String description,
     IssueStatus status,
     IssueSeverity severity,
     String imageUrl,
     Location location,
     String assignedBy,
     String assignedTo,
     Instant createdAt,
     Instant assignedAt,
     Instant resolvedAt,
     String solutionImageUrl,

     ComplaintCommunityInteractionDto communityDetails
){}