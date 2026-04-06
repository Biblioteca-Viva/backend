package org.bibliotecaviva.backend.application.dtos.request;

import org.bibliotecaviva.backend.domain.enums.Role;

public record   RegisterRequestDTO(String name, String email, String password, Role role) {}
