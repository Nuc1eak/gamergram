package com.nikita.gamergram.dto;

import com.nikita.gamergram.entity.enums.MediaType;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        Long authorId,
        String authorUsername,
        String authorAvatarUrl,
        Long gameId,
        String gameTitle,
        String mediaUrl,
        MediaType mediaType,
        String caption,
        LocalDateTime createdAt,
        long likeCount,
        long commentCount,
        boolean likedByMe
) {
}
