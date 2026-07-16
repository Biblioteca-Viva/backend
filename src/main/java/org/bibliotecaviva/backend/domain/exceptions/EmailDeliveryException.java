package org.bibliotecaviva.backend.domain.exceptions;

import org.springframework.http.HttpStatus;

public class EmailDeliveryException extends ApiErrorException {

    public EmailDeliveryException() {
        super("Não foi possível enviar o email de redefinição de senha", HttpStatus.BAD_GATEWAY);
    }
}
