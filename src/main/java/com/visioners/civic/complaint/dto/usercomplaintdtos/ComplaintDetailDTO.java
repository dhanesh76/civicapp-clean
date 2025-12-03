package com.visioners.civic.complaint.dto.usercomplaintdtos;

import java.time.Instant;

import com.visioners.civic.community.dto.ComplaintCommunityDetailDTO;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ComplaintDetailDTO {
    private final String complaintId;
    private final String description;
    private final IssueStatus status;
    private final IssueSeverity severity;
    private final Location location;
    private final String imageUrl;
    private final String audioUrl;
    private final Instant createdAt;
    private final Instant assignedAt;
    private final Instant resolvedAt;
    private final String solutionNote;
    private final String solutionImageUrl;
    private final com.visioners.civic.complaint.dto.feedback.ViewFeedbackDTO feedback;
    private final ComplaintCommunityDetailDTO communityDetail;
}

