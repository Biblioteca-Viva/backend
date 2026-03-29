package org.bibliotecaviva.backend.domain.exceptions;

public class WorkNotFoundException extends RuntimeException {
    public WorkNotFoundException(String message) {
        super(message);
    }
}
