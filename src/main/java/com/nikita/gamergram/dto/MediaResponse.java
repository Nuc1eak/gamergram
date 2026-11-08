package com.nikita.gamergram.dto;

import com.nikita.gamergram.entity.enums.MediaType;

public record MediaResponse(String url, MediaType mediaType) {
}
