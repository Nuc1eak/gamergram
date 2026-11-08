package com.nikita.gamergram.service;

import com.nikita.gamergram.dto.CreateGameRequest;
import com.nikita.gamergram.dto.GameResponse;
import com.nikita.gamergram.entity.Game;
import com.nikita.gamergram.exception.GameNotFoundException;
import com.nikita.gamergram.repository.GameRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    void create_savesGame_andReturnsResponse() {
        CreateGameRequest request = new CreateGameRequest("Counter-Strike 2", "Tactical shooter");
        Game saved = Game.builder().id(1L).title("Counter-Strike 2").description("Tactical shooter").build();
        when(gameRepository.save(any(Game.class))).thenReturn(saved);

        GameResponse response = gameService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Counter-Strike 2");
        assertThat(response.description()).isEqualTo("Tactical shooter");
    }

    @Test
    void getById_returnsGame_whenExists() {
        Game game = Game.builder().id(5L).title("Dota 2").description("MOBA").build();
        when(gameRepository.findById(5L)).thenReturn(Optional.of(game));

        GameResponse response = gameService.getById(5L);

        assertThat(response.title()).isEqualTo("Dota 2");
    }

    @Test
    void getById_throws_whenNotFound() {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getById(99L))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void search_returnsMappedList() {
        Game game = Game.builder().id(1L).title("Valorant").description("Shooter").build();
        when(gameRepository.findByTitleContainingIgnoreCase("val")).thenReturn(List.of(game));

        List<GameResponse> results = gameService.search("val");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo("Valorant");
    }
}
