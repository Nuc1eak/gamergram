package com.nikita.gamergram.exception;

// Common parent so GlobalExceptionHandler needs a single handler.
public abstract class NotFoundException extends RuntimeException {

    protected NotFoundException(String message) {
        super(message);
    }
}
