package org.bibliotecaviva.backend.domain.exceptions;

import org.springframework.http.HttpStatus;

public abstract class ApiErrorException extends RuntimeException {

    private final HttpStatus status;
    protected ApiErrorException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() { return status; }
}
