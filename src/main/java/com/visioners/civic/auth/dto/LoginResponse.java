package com.visioners.civic.auth.dto;

import java.time.Instant;

import lombok.Builder;

@Builder
public record LoginResponse(String loginId, Long userId, String accessToken, String refreshToken, Instant timestamp) {
} 
