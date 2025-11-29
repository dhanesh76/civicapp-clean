package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReopenAssignDTO {
    @NotNull(message = "workerId is required")
    private Long workerId;
}
