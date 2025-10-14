package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignComplaintDTO {
    private Long complaintId;
    private Long workerId;
}

