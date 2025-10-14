package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RejectComplaintDto(
    @NotNull long complaintId, 
    @NotBlank String rejectionNote
) {

}
