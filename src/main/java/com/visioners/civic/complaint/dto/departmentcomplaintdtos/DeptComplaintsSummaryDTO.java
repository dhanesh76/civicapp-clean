package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import java.time.Instant;

import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;

import lombok.Builder;

@Builder
public record DeptComplaintsSummaryDTO(
    String complaintId,
    String description,
    String category,
    String subCategory,
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

    long supportCount,
    long commentCount
){}
