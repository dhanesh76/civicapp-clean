package com.visioners.civic.complaint.dto.usercomplaintdtos;

import java.time.Instant;

import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;

public record ComplaintRaiseResponse(
    String complaintId,
    String department,
    IssueSeverity severity,
    IssueStatus status,
    Instant createdAt
) {
}
