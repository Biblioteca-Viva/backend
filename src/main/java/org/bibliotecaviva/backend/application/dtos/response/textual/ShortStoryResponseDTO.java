package org.bibliotecaviva.backend.application.dtos.response.textual;

import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShortStoryResponseDTO(
        UUID id,
        String title,
        String author,
        LocalDateTime publicationDate,
        String description,
        String type,
        String content
) implements WorkResponse {
}
