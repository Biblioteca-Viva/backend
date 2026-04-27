package org.bibliotecaviva.backend.application.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @Email @NotBlank String email,
        @Size(min = 6, message = "A senha deve conter no mínimo 6 caracteres") @NotBlank String password
) {
}
