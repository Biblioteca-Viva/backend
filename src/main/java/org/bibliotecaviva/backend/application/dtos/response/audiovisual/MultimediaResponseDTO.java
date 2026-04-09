package org.bibliotecaviva.backend.application.dtos.response.audiovisual;

import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record MultimediaResponseDTO(
        UUID id,
        String title,
        String author,
        LocalDateTime publicationDate,
        String description,
        String type,
        String url,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        Duration duration


) implements WorkResponse {
}