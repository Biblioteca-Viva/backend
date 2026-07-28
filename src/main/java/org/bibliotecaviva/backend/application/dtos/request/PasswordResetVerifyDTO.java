package org.bibliotecaviva.backend.application.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetVerifyDTO(
        @NotBlank @Email String email,
        @NotBlank
        @Pattern(regexp = "\\d{8}", message = "O codigo deve conter exatamente 8 digitos")
        String code
) {
}
