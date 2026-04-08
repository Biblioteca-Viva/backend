package org.bibliotecaviva.backend.application.dtos.response.textual;

import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record CordelResponseDTO(
        UUID id,
        String title,
        String author,
        LocalDateTime publicationDate,
        String description,
        String type,
        String content,
        String rhymeScheme,
        Long viewCount,
        Long likeCount
) implements WorkResponse {
}
