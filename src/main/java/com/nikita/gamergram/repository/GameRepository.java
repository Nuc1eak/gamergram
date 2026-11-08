package com.nikita.gamergram.repository;

import com.nikita.gamergram.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    // Title autocomplete.
    List<Game> findByTitleContainingIgnoreCase(String title);
}
