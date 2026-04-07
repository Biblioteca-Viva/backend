package org.bibliotecaviva.backend.domain.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiErrorException {
    protected BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
