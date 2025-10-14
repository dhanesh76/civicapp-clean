package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import java.time.Instant;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.Location;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComplaintViewDTO {
    private Long id;
    private String description;
    private IssueStatus status;
    private IssueSeverity severity;
    private Location location;
    private String assignedBy;
    private String assignedTo;
    private Instant createdAt;
    private Instant assignedAt;
    private Instant resolvedAt;
    private String solutionNote;
    private String solutionImageUrl;
    private String rejectionNote;
}

