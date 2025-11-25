package com.visioners.civic.auth.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

import com.visioners.civic.auth.model.OtpPurpose;

public record OtpRequest(

    @NotBlank(message = "Mobile number cannot be empty")
    @Pattern(regexp = "[0-9]{10}", message = "Enter a valid 10-digit mobile number")
    String mobileNumber,

    @NotNull(message = "Purpose cannot be null")
    OtpPurpose purpose
) {}
