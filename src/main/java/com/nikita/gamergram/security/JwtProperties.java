package com.nikita.gamergram.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Typed binding for the jwt.* properties.
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long expirationMs) {
}
