package com.visioners.civic.complaint.dto.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateFeedbackDTO(
    
    @Min(0) @Max(5)
    Double rating,
    
    @NotBlank
    String comment
) {}