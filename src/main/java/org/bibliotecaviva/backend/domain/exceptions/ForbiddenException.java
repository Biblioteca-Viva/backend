package org.bibliotecaviva.backend.domain.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiErrorException {
    public ForbiddenException(String message) {
        super(message,HttpStatus.FORBIDDEN);
    }
}
