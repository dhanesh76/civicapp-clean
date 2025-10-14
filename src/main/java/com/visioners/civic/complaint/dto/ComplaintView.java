package com.visioners.civic.complaint.dto;

import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;
import lombok.Builder;

@Builder
public record ComplaintView(
    Long id,
    String raidedBy,
    String imageUrl,
    IssueStatus status,
    IssueSeverity severity,
    Location location,
    String assignedBy,
    String assignedTo,
    String solutionImageUrl,
    String solutionNote
) {}
