package org.bibliotecaviva.backend.application.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "WorkResponseDTO", description = "DTO with generic informations")
public record WorkResponseDTO(
        UUID id,
        String title,
        String author,
        LocalDateTime publicationDate,
        String description,
        String type
) implements WorkResponse {
}

