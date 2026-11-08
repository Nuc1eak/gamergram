package com.nikita.gamergram.integration;

import com.nikita.gamergram.entity.Game;
import com.nikita.gamergram.entity.User;
import com.nikita.gamergram.entity.UserGame;
import com.nikita.gamergram.entity.enums.GameRelationType;
import com.nikita.gamergram.repository.GameRepository;
import com.nikita.gamergram.repository.UserGameRepository;
import com.nikita.gamergram.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserGameRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private UserGameRepository userGameRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;

    @Test
    void findByUserIdAndRelationType_returnsOnlyMatchingRelation() {
        User user = userRepository.save(user("gamer"));
        Game favGame = gameRepository.save(game("Hades"));
        Game wishGame = gameRepository.save(game("Elden Ring"));

        userGameRepository.save(userGame(user, favGame, GameRelationType.FAVORITE));
        userGameRepository.save(userGame(user, wishGame, GameRelationType.WISHLIST));

        List<UserGame> favorites = userGameRepository.findByUserIdAndRelationType(user.getId(), GameRelationType.FAVORITE);

        assertThat(favorites).hasSize(1);
        assertThat(favorites.get(0).getGame().getTitle()).isEqualTo("Hades");
    }

    @Test
    void findByUserIdAndGameId_returnsExistingLink() {
        User user = userRepository.save(user("gamer2"));
        Game game = gameRepository.save(game("Dota 2"));
        userGameRepository.save(userGame(user, game, GameRelationType.FAVORITE));

        Optional<UserGame> found = userGameRepository.findByUserIdAndGameId(user.getId(), game.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRelationType()).isEqualTo(GameRelationType.FAVORITE);
    }

    @Test
    void uniqueConstraint_rejectsDuplicateUserGameLink_onRealPostgres() {
        User user = userRepository.save(user("gamer3"));
        Game game = gameRepository.save(game("Valorant"));
        userGameRepository.save(userGame(user, game, GameRelationType.FAVORITE));

        UserGame duplicate = userGame(user, game, GameRelationType.WISHLIST);

        assertThatThrownBy(() -> userGameRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User user(String username) {
        return User.builder().username(username).email(username + "@test.com").passwordHash("hash").build();
    }

    private Game game(String title) {
        return Game.builder().title(title).build();
    }

    private UserGame userGame(User user, Game game, GameRelationType type) {
        return UserGame.builder().user(user).game(game).relationType(type).build();
    }
}
