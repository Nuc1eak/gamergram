package com.nikita.gamergram.dto;

import java.time.LocalDateTime;

// Explicit field list, so new sensitive User fields never leak into the API.
public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String avatarUrl,
        String bio,
        LocalDateTime createdAt
) {
}
