package org.bibliotecaviva.backend.application.dtos.response.textual;

import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;
import org.bibliotecaviva.backend.application.dtos.response.visual.IllustrationResponseDTO;

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
        Long likeCount,
        Long commentCount,
        IllustrationResponseDTO illustration,
        String studentClass

) implements WorkResponse {
}
