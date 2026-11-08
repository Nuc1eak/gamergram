package com.nikita.gamergram.controller;

import com.nikita.gamergram.dto.AuthResponse;
import com.nikita.gamergram.dto.LoginRequest;
import com.nikita.gamergram.dto.UserProfileResponse;
import com.nikita.gamergram.dto.UserRegistrationRequest;
import com.nikita.gamergram.service.AuthService;
import com.nikita.gamergram.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Matches permitAll for /api/auth/**: the only controller reachable without a token.
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        UserProfileResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
