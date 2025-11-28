package com.visioners.civic.community.dto;

import java.util.List;

//used for the community tab purpose
public record ComplaintCommunityInteractionDto(
    long supportCount,
    long commentCount,
    boolean supporttedByUser,
    List<CommunityCommentDTO> comments
) {
}