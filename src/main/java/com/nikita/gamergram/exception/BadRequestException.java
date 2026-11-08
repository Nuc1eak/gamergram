package com.nikita.gamergram.exception;

// Service-level validation that is neither Bean Validation nor "not found".
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
