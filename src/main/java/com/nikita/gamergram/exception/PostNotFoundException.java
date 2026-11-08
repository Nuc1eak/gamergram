package com.nikita.gamergram.exception;

public class PostNotFoundException extends NotFoundException {

    public PostNotFoundException(String message) {
        super(message);
    }

    public static PostNotFoundException byId(Long id) {
        return new PostNotFoundException("Post not found: id=" + id);
    }
}
