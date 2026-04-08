package org.bibliotecaviva.backend.application.dtos.response;

import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link org.bibliotecaviva.backend.domain.entities.User}
 */
public record UserResponseDTO(UUID id, String name, String email, Role role,
                              Status accountStatus) implements Serializable {
}