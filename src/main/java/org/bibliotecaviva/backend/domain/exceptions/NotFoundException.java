package org.bibliotecaviva.backend.domain.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiErrorException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
