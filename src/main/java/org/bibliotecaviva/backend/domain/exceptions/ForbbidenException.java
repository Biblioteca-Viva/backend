package org.bibliotecaviva.backend.domain.exceptions;

import org.springframework.http.HttpStatus;

public class ForbbidenException extends ApiErrorException {
    public ForbbidenException(String message) {
        super(message,HttpStatus.FORBIDDEN);
    }
}
