package com.nikita.gamergram.controller;

import com.nikita.gamergram.dto.AddUserGameRequest;
import com.nikita.gamergram.dto.UserGameResponse;
import com.nikita.gamergram.entity.enums.GameRelationType;
import com.nikita.gamergram.security.UserPrincipal;
import com.nikita.gamergram.service.UserGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserGameController {

    private final UserGameService userGameService;

    // Own games only: the user id comes from the principal, not the URL.
    @PostMapping("/api/me/games")
    public ResponseEntity<Void> addGame(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddUserGameRequest request
    ) {
        userGameService.addGame(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/me/games/{gameId}")
    public ResponseEntity<Void> removeGame(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long gameId
    ) {
        userGameService.removeGame(principal.getId(), gameId);
        return ResponseEntity.noContent().build();
    }

    // Any user's lists are public profile data.
    @GetMapping("/api/users/{userId}/games")
    public List<UserGameResponse> getUserGames(
            @PathVariable Long userId,
            @RequestParam GameRelationType type
    ) {
        return userGameService.getUserGames(userId, type);
    }
}
