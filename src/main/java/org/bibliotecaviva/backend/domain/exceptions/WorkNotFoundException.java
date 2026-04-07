package org.bibliotecaviva.backend.domain.exceptions;

public class WorkNotFoundException extends BadRequestException {
    public WorkNotFoundException(String message) {
        super(message);
    }
}
