package com.visioners.civic.complaint.dto.usercomplaintdtos;

import java.time.Instant;

import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;

import lombok.Builder;

@Builder
public record ComplaintDetailDTO(
    Long id,
    String description,
    IssueStatus status,
    IssueSeverity severity,
    Location location,
    String imageUrl,
    Instant createdAt,
    Instant assignedAt,
    Instant resolvedAt,
    String solutionNote,
    String solutionImageUrl
) {}

