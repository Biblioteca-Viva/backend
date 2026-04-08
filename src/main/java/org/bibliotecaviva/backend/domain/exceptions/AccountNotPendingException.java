package org.bibliotecaviva.backend.domain.exceptions;

public class AccountNotPendingException extends BadRequestException {
    public AccountNotPendingException(String message) {
        super(message);
    }
}
