package com.nikita.gamergram.exception;

// Unchecked: an expected business case, handled once by GlobalExceptionHandler.
public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException byId(Long id) {
        return new UserNotFoundException("User not found: id=" + id);
    }

    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("User not found: username=" + username);
    }
}
