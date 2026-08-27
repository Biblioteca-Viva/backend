package org.bibliotecaviva.backend.domain.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiErrorException {

    public InvalidRefreshTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public InvalidRefreshTokenException() {
        super("Refresh token inválido ou expirado", HttpStatus.UNAUTHORIZED);
    }
}
