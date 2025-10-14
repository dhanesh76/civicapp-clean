package com.visioners.civic.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
    
    @NotBlank(message = "mobile number cannot be empty")
    @Pattern(regexp="(^$|[0-9]{10})", message = "enter a valid number")
    String mobileNumber,
    
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must include upper, lower, number, and special character"
    )
    String password
) {
}
