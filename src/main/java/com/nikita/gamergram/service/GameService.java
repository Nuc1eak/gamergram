package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.CreateGameRequest;
import com.nikita.gamergram.dto.GameResponse;
import com.nikita.gamergram.entity.Game;
import com.nikita.gamergram.exception.GameNotFoundException;
import com.nikita.gamergram.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    @Transactional
    public GameResponse create(CreateGameRequest request) {
        Game game = Game.builder()
                .title(request.title())
                .description(request.description())
                .build();

        Game saved = gameRepository.save(game);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GameResponse getById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> GameNotFoundException.byId(id));
        return toResponse(game);
    }

    @Transactional(readOnly = true)
    public List<GameResponse> search(String title) {
        return gameRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::toResponse)
                .toList();
    }

    private GameResponse toResponse(Game game) {
        return new GameResponse(game.getId(), game.getTitle(), game.getDescription());
    }
}
