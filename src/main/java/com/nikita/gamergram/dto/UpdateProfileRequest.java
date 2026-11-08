package com.nikita.gamergram.dto;

import jakarta.validation.constraints.Size;

// null keeps a field, blank clears it.
public record UpdateProfileRequest(
        @Size(max = 255)
        String avatarUrl,

        @Size(max = 500)
        String bio
) {
}
