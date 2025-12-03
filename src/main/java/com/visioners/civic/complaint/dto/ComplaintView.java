package com.visioners.civic.complaint.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;
import lombok.Builder;

@Builder
public record ComplaintView(

    @NotBlank(message = "Complaint ID cannot be empty")
    String complaintId,

    @NotBlank(message = "RaidedBy cannot be empty")
    String raidedBy,

    String imageUrl,

    @NotNull(message = "Status cannot be null")
    IssueStatus status,

    @NotNull(message = "Severity cannot be null")
    IssueSeverity severity,

    @NotNull(message = "Location cannot be null")
    Location location,

    String assignedBy,
    String assignedTo,
    String solutionImageUrl,
    String solutionNote
) {}
