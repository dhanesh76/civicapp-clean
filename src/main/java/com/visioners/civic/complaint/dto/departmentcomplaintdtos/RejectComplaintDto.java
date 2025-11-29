package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import jakarta.validation.constraints.NotBlank;

public record RejectComplaintDto(
    @NotBlank(message = "complaintId is required") String complaintId,
    @NotBlank(message = "rejectionNote cannot be blank") String rejectionNote,
    // optional reopen id when rejecting a reopen request
    String reopenId
) {

}
