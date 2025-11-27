package com.visioners.civic.complaint.dto.feedback;

import java.time.Instant;

public record ViewFeedbackDTO(
    String comment,
    Double rating, 
    Instant createdAt    
){}
