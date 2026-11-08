package com.nikita.gamergram.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGameRequest(

        @NotBlank(message = "title is required")
        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description
) {
}
