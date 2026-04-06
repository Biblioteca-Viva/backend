package org.bibliotecaviva.backend.application.dtos.response.visual;

import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record InfographicReponseDTO(
        UUID id,
        String title,
        String author,
        LocalDateTime publicationDate,
        String description,
        String type,
        String url
) implements WorkResponse {
}
