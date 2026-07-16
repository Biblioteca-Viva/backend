package org.bibliotecaviva.backend.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmDTO(
        @NotBlank String resetToken,
        @NotBlank
        @Size(min = 6, message = "A senha deve conter no mínimo 6 caracteres")
        String newPassword
) {
}
