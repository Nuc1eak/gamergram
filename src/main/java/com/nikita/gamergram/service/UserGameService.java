package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.AddUserGameRequest;
import com.nikita.gamergram.dto.GameResponse;
import com.nikita.gamergram.dto.UserGameResponse;
import com.nikita.gamergram.entity.Game;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.entity.UserGame;
import com.nikita.gamergram.entity.enums.GameRelationType;
import com.nikita.gamergram.exception.GameNotFoundException;
import com.nikita.gamergram.repository.GameRepository;
import com.nikita.gamergram.repository.UserGameRepository;
import com.nikita.gamergram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserGameService {

    private final UserGameRepository userGameRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    @Transactional
    public void addGame(Long userId, AddUserGameRequest request) {
        Long gameId = request.gameId();

        userGameRepository.findByUserIdAndGameId(userId, gameId).ifPresentOrElse(
                // Moving between lists: dirty checking persists the change, no save() needed.
                existing -> existing.setRelationType(request.relationType()),
                () -> {
                    Game gameRef = gameRepository.findById(gameId)
                            .orElseThrow(() -> GameNotFoundException.byId(gameId));
                    // The id comes from SecurityContext, so the user exists; skip the SELECT.
                    User userRef = userRepository.getReferenceById(userId);

                    UserGame userGame = UserGame.builder()
                            .user(userRef)
                            .game(gameRef)
                            .relationType(request.relationType())
                            .build();
                    userGameRepository.save(userGame);
                }
        );
    }

    @Transactional
    public void removeGame(Long userId, Long gameId) {
        userGameRepository.deleteByUserIdAndGameId(userId, gameId);
    }

    @Transactional(readOnly = true)
    public List<UserGameResponse> getUserGames(Long userId, GameRelationType relationType) {
        return userGameRepository.findByUserIdAndRelationType(userId, relationType).stream()
                .map(this::toResponse)
                .toList();
    }

    private UserGameResponse toResponse(UserGame userGame) {
        Game game = userGame.getGame();
        GameResponse gameResponse = new GameResponse(game.getId(), game.getTitle(), game.getDescription());
        return new UserGameResponse(userGame.getId(), gameResponse, userGame.getRelationType());
    }
}
