package org.bibliotecaviva.backend.application.dto;

import org.bibliotecaviva.backend.domain.enums.Role;

public record LoginResponseDTO(String token, String email, Role role) {}
