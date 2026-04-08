package org.bibliotecaviva.backend.domain.exceptions;

public class AccountAlreadyBlockedException extends BadRequestException {
    public AccountAlreadyBlockedException(String message) {
        super(message);
    }
}
