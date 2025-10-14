package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentComplaintStatisticsDTO {
    private long totalComplaints;
    private long openCount;
    private long assignedCount;
    private long resolvedCount;
    private long rejectedCount;
    private long closedCount;
}
