package org.bibliotecaviva.backend.application.dtos.response;

import org.bibliotecaviva.backend.domain.enums.Role;

import java.util.UUID;

public record UserProfileResponseDTO(
        UUID id,
        String name,
        String email
) {
}
