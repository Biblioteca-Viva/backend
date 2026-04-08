package org.bibliotecaviva.backend.domain.exceptions;

public class UserAlreadyExistsException extends ConflictException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
