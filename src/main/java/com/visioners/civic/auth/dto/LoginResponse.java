package com.visioners.civic.auth.dto;

import java.time.Instant;

import lombok.Builder;

@Builder
public record LoginResponse(String loginId, String accessToken, String refreshToken, Instant timestamp) {

} 
