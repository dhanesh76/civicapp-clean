package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignComplaintDTO {
    @NotNull
    private String complaintId;

    
    @NotNull
    private Long workerId;
}

