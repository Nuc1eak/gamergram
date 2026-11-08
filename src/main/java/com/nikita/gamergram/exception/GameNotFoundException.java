package com.nikita.gamergram.exception;

public class GameNotFoundException extends NotFoundException {

    public GameNotFoundException(String message) {
        super(message);
    }

    public static GameNotFoundException byId(Long id) {
        return new GameNotFoundException("Game not found: id=" + id);
    }
}
