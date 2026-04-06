package org.bibliotecaviva.backend.application.dtos.response.textual;

import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record EssayResponseDTO(
        UUID id,
        String title,
        String author,
        LocalDateTime publicationDate,
        String description,
        String type,
        String content,
        Integer rate,
        String theme,
        String themeDescription,
        String feedback
) implements WorkResponse {
}
