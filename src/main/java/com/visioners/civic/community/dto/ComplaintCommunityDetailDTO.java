package com.visioners.civic.community.dto;

import java.util.List;

import lombok.Builder;
//used for the officer and user view purposes
@Builder
public record ComplaintCommunityDetailDTO(
    long supportCount,
    long commentCount,
    List<CommunityCommentDTO> comments
) {}
