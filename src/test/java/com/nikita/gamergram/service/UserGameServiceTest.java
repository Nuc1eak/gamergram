package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.AddUserGameRequest;
import com.nikita.gamergram.dto.UserGameResponse;
import com.nikita.gamergram.entity.Game;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.entity.UserGame;
import com.nikita.gamergram.entity.enums.GameRelationType;
import com.nikita.gamergram.exception.GameNotFoundException;
import com.nikita.gamergram.repository.GameRepository;
import com.nikita.gamergram.repository.UserGameRepository;
import com.nikita.gamergram.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGameServiceTest {

    @Mock
    private UserGameRepository userGameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private UserGameService userGameService;

    @Test
    void addGame_createsNewEntry_whenNotAlreadyLinked() {
        AddUserGameRequest request = new AddUserGameRequest(10L, GameRelationType.FAVORITE);
        Game game = Game.builder().id(10L).title("Elden Ring").build();
        User userRef = User.builder().id(1L).build();

        when(userGameRepository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.empty());
        when(gameRepository.findById(10L)).thenReturn(Optional.of(game));
        when(userRepository.getReferenceById(1L)).thenReturn(userRef);

        userGameService.addGame(1L, request);

        verify(userGameRepository).save(any(UserGame.class));
    }

    @Test
    void addGame_throws_whenGameDoesNotExist() {
        AddUserGameRequest request = new AddUserGameRequest(404L, GameRelationType.WISHLIST);
        when(userGameRepository.findByUserIdAndGameId(1L, 404L)).thenReturn(Optional.empty());
        when(gameRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userGameService.addGame(1L, request))
                .isInstanceOf(GameNotFoundException.class);

        verify(userGameRepository, never()).save(any());
    }

    @Test
    void addGame_updatesRelationType_whenAlreadyLinked() {
        // Already a FAVORITE; the user moves it to WISHLIST.
        UserGame existing = UserGame.builder()
                .id(7L)
                .relationType(GameRelationType.FAVORITE)
                .build();
        AddUserGameRequest request = new AddUserGameRequest(10L, GameRelationType.WISHLIST);
        when(userGameRepository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.of(existing));

        userGameService.addGame(1L, request);

        // Persisted by dirty checking, not an explicit save().
        assertThat(existing.getRelationType()).isEqualTo(GameRelationType.WISHLIST);
        verify(userGameRepository, never()).save(any());
        verify(gameRepository, never()).findById(any());
    }

    @Test
    void removeGame_delegatesToRepository() {
        userGameService.removeGame(1L, 10L);

        verify(userGameRepository).deleteByUserIdAndGameId(1L, 10L);
    }

    @Test
    void getUserGames_returnsMappedList() {
        Game game = Game.builder().id(10L).title("Hades").description("Roguelike").build();
        UserGame userGame = UserGame.builder().id(3L).game(game).relationType(GameRelationType.FAVORITE).build();

        when(userGameRepository.findByUserIdAndRelationType(1L, GameRelationType.FAVORITE))
                .thenReturn(List.of(userGame));

        List<UserGameResponse> result = userGameService.getUserGames(1L, GameRelationType.FAVORITE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).game().title()).isEqualTo("Hades");
        assertThat(result.get(0).relationType()).isEqualTo(GameRelationType.FAVORITE);
    }
}
