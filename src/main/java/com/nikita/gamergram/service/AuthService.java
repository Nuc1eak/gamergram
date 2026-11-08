package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.AuthResponse;
import com.nikita.gamergram.dto.LoginRequest;
import com.nikita.gamergram.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        // Verifies the password and throws BadCredentialsException on mismatch.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtService.generateToken(authentication.getName());
        return AuthResponse.bearer(token);
    }
}
