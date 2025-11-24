package com.visioners.civic.complaint.dto.usercomplaintdtos;


import java.time.Instant;

import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;

import lombok.Builder;

@Builder
public record ComplaintRaiseResponseDTO(
    String complaintId,
    String department,
    IssueSeverity severity,
    IssueStatus status,
    Instant createdAt
) {}

