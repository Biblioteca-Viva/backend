package org.bibliotecaviva.backend.domain.exceptions;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends ApiErrorException {

    public TooManyRequestsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
