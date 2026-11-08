package com.nikita.gamergram.dto;

import com.nikita.gamergram.entity.enums.GameRelationType;

public record UserGameResponse(Long id, GameResponse game, GameRelationType relationType) {
}
