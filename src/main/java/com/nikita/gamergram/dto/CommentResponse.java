package com.nikita.gamergram.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long authorId,
        String authorUsername,
        String authorAvatarUrl,
        String body,
        LocalDateTime createdAt
) {
}
