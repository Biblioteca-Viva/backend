package org.bibliotecaviva.backend.application.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Size(min = 2, max = 100, message = "O nome deve conter entre 2 e 100 caracteres")
        String name,
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 6, message = "A senha deve conter no mínimo 6 caracteres")
        String password
) {
}
