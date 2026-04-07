package org.bibliotecaviva.backend.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConflictException extends ApiErrorException {
    public ConflictException(String message) {
        super(message,HttpStatus.CONFLICT);
    }
}
