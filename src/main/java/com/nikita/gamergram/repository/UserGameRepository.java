package com.nikita.gamergram.repository;

import com.nikita.gamergram.entity.UserGame;
import com.nikita.gamergram.entity.enums.GameRelationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {

    // A user's favorites or wishlist.
    List<UserGame> findByUserIdAndRelationType(Long userId, GameRelationType relationType);

    Optional<UserGame> findByUserIdAndGameId(Long userId, Long gameId);

    boolean existsByUserIdAndGameId(Long userId, Long gameId);

    void deleteByUserIdAndGameId(Long userId, Long gameId);
}
