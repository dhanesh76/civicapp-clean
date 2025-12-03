package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReopenRejectDTO {
    @NotBlank(message = "rejectionNote cannot be blank")
    private String rejectionNote;
}
