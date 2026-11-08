package com.nikita.gamergram.controller;

import com.nikita.gamergram.dto.CreateGameRequest;
import com.nikita.gamergram.dto.GameResponse;
import com.nikita.gamergram.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    // Any authenticated user can add games: no moderator role in the MVP.
    @PostMapping
    public ResponseEntity<GameResponse> create(@Valid @RequestBody CreateGameRequest request) {
        GameResponse response = gameService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public GameResponse getById(@PathVariable Long id) {
        return gameService.getById(id);
    }

    @GetMapping
    public List<GameResponse> search(@RequestParam(defaultValue = "") String title) {
        return gameService.search(title);
    }
}
