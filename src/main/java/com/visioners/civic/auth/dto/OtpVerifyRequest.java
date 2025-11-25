package com.visioners.civic.auth.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import com.visioners.civic.auth.model.OtpPurpose;

public record OtpVerifyRequest(

    @NotBlank(message = "Mobile number cannot be empty")
    @Pattern(regexp = "[0-9]{10}", message = "Enter a valid 10-digit mobile number")
    String mobileNumber,

    @NotBlank(message = "OTP cannot be empty")
    @Size(min = 4, max = 8, message = "OTP must be between 4 and 8 digits")
    String otp,

    @NotNull(message = "Purpose cannot be null")
    OtpPurpose purpose
) {}
