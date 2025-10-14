package com.visioners.civic.auth.dto;

import com.visioners.civic.auth.model.OtpPurpose;

public record OtpVerifyRequest(
    String mobileNumber,
    String otp,
    OtpPurpose purpose
) {}
