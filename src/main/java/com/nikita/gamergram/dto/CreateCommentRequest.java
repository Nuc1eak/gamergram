package com.nikita.gamergram.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(

        @NotBlank(message = "body is required")
        @Size(max = 1000)
        String body
) {
}
