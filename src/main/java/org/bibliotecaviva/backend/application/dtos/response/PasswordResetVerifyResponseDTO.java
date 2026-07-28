package org.bibliotecaviva.backend.application.dtos.response;

public record PasswordResetVerifyResponseDTO(
        String resetToken,
        long expiresInSeconds
) {
}
