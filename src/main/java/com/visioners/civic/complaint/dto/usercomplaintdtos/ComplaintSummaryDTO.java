package com.visioners.civic.complaint.dto.usercomplaintdtos;

import java.time.Instant;

import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;

import lombok.Builder;

@Builder
public record ComplaintSummaryDTO(
    Long id,
    IssueStatus status,
    IssueSeverity severity,
    Instant createdAt,
    Location location
) {}
