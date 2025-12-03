package com.visioners.civic.complaint.dto.fieldworkerdtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveComplaintDto(
    @NotBlank(message = "complaintId is required")
    String complaintId,

    
    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "longitude must be >= -180 and <= 180")
    @DecimalMax(value = "180.0", message = "longitude must be >= -180 and <= 180")
    Double lon,
    
    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0", message = "latitude must be >= -90 and <= 90")
    @DecimalMax(value = "90.0", message = "latitude must be >= -90 and <= 90")
    Double lat,

    @NotBlank(message = "solutionNote cannot be empty")
    @Size(max = 2000, message = "solutionNote cannot exceed 2000 characters")
    String solutionNote,
    // optional reopen id when resolving in context of a reopen
    String reopenId) {
}
 

