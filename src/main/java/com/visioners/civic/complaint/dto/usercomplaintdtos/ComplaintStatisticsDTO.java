package com.visioners.civic.complaint.dto.usercomplaintdtos;

import lombok.Builder;

@Builder
public record ComplaintStatisticsDTO(
    Long totalComplaints,
    Long openCount,
    Long assignedCount,
    Long resolvedCount,
    Long closedCount
) {}

