package org.bibliotecaviva.backend.application.dtos.response;

import org.bibliotecaviva.backend.domain.enums.Role;

import java.util.UUID;

public record LoginResponseDTO(
        String accessToken,
        UUID id,
        String name,
        String email,
        Role role
) {
}
