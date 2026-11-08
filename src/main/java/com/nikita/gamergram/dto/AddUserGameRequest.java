package com.nikita.gamergram.dto;

import com.nikita.gamergram.entity.enums.GameRelationType;
import jakarta.validation.constraints.NotNull;

public record AddUserGameRequest(

        @NotNull(message = "gameId is required")
        Long gameId,

        @NotNull(message = "relationType is required")
        GameRelationType relationType
) {
}
