package org.bibliotecaviva.backend.domain.exceptions;

public class ReplyAlreadyExistsException extends BadRequestException {
    public ReplyAlreadyExistsException(String message) {
        super(message);
    }
}

