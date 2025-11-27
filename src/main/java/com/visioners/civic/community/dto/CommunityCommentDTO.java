package com.visioners.civic.community.dto;

import java.time.Instant;

public record CommunityCommentDTO(
    String username,
    String comment,
    Instant createdAt
) {}
