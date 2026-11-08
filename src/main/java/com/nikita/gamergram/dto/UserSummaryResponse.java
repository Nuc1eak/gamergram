package com.nikita.gamergram.dto;

// Public user summary for lists. No email or bio.
public record UserSummaryResponse(Long id, String username, String avatarUrl) {
}
