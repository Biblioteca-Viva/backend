package org.bibliotecaviva.backend.domain.exceptions;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
