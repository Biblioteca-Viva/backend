package org.bibliotecaviva.backend.domain.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidPasswordResetException extends ApiErrorException {

    public InvalidPasswordResetException() {
        super("Código ou token de redefinicao inválido ou expirado", HttpStatus.BAD_REQUEST);
    }
}
