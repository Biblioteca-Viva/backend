package org.bibliotecaviva.backend.domain.exceptions;

public class UserNotFoundException extends BadRequestException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
