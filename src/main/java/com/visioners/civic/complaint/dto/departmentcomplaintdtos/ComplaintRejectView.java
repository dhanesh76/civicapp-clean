package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;

import lombok.Builder;
@Builder
public record ComplaintRejectView(
    Long id,
    String raisedBy,
    String imageUrl,
    IssueStatus status,
    IssueSeverity severity,
    Location location,
    String assignedBy,
    String assignedTo,
    String solutionImageUrl,
    String solutionNote,
    String rejectionNote
) {}
