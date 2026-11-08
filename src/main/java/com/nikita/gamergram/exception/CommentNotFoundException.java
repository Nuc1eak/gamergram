package com.nikita.gamergram.exception;

public class CommentNotFoundException extends NotFoundException {

    public CommentNotFoundException(String message) {
        super(message);
    }

    public static CommentNotFoundException byId(Long id) {
        return new CommentNotFoundException("Comment not found: id=" + id);
    }
}
