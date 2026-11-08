package com.nikita.gamergram.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// No mocks: this class is the crypto logic, so it runs against real jjwt.
class JwtServiceTest {

    // At least 32 bytes: HS256 needs 256 bits or Keys.hmacShaKeyFor throws.
    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-32-bytes-long";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(TEST_SECRET, 60_000));
    }

    @Test
    void generateToken_thenExtractUsername_roundTrips() {
        String token = jwtService.generateToken("nikita");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("nikita");
    }

    @Test
    void isTokenValid_returnsTrue_whenUsernameMatchesAndNotExpired() {
        String token = jwtService.generateToken("nikita");

        assertThat(jwtService.isTokenValid(token, springUser("nikita"))).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_whenUsernameDoesNotMatch() {
        String token = jwtService.generateToken("nikita");

        assertThat(jwtService.isTokenValid(token, springUser("someone-else"))).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_whenTokenExpired() {
        // Negative expiration keeps the test deterministic, with no Thread.sleep().
        JwtService shortLivedJwtService = new JwtService(new JwtProperties(TEST_SECRET, -1_000));
        String expiredToken = shortLivedJwtService.generateToken("nikita");

        assertThat(shortLivedJwtService.isTokenValid(expiredToken, springUser("nikita"))).isFalse();
    }

    @Test
    void extractUsername_throws_forGarbageToken() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-real-jwt"))
                .isInstanceOf(JwtException.class);
    }

    private UserDetails springUser(String username) {
        return User.withUsername(username)
                .password("irrelevant")
                .authorities("ROLE_USER")
                .build();
    }
}
