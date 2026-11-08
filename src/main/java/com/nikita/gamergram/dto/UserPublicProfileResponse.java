package com.nikita.gamergram.dto;

// Profile page data. Counters are kept out of the lightweight UserSummaryResponse.
public record UserPublicProfileResponse(
        Long id,
        String username,
        String avatarUrl,
        String bio,
        long postCount,
        long followerCount,
        long followingCount,
        boolean followedByMe
) {
}
