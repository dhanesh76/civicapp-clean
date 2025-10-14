package com.visioners.civic.auth.model;

public record OtpData(
    String otp,
    OtpPurpose purpose
) {}
