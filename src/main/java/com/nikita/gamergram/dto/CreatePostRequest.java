package com.nikita.gamergram.dto;

import com.nikita.gamergram.entity.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(

        @NotBlank(message = "mediaUrl is required")
        String mediaUrl,

        @NotNull(message = "mediaType is required")
        MediaType mediaType,

        @Size(max = 2200)
        String caption,

        // Optional game tag.
        Long gameId
) {
}
