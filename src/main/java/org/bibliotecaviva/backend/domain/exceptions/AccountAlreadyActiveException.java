package org.bibliotecaviva.backend.domain.exceptions;

public class AccountAlreadyActiveException extends BadRequestException {
    public AccountAlreadyActiveException(String message) {
        super(message);
    }
}
