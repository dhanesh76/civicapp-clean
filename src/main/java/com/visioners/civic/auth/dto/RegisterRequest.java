package com.visioners.civic.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    

    @NotBlank(message = "Mobile number cannot be empty")
    @Pattern(regexp = "[0-9]{10}", message = "Enter a valid 10-digit mobile number")
    String mobileNumber,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must include upper, lower, number, and special character"
    )
    String password
) {}
