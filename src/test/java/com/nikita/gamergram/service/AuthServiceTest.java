package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.AuthResponse;
import com.nikita.gamergram.dto.LoginRequest;
import com.nikita.gamergram.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("nikita", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("nikita");
        when(jwtService.generateToken("nikita")).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_propagatesException_whenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("nikita", "wrong-password");

        // AuthenticationManager checks the password; AuthService must not swallow the exception.
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
