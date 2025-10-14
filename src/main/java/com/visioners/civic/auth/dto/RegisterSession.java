package com.visioners.civic.auth.dto;

/**
 * Temp object stored in Redis until OTP verification.
 */
public record RegisterSession(
    String mobileNumber,
    String encodedPassword,
    String role
) {}
