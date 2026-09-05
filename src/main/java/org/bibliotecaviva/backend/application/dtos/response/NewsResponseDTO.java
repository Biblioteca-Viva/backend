package org.bibliotecaviva.backend.application.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NewsResponseDTO(
        UUID id,
        String title,
        String content,
        String imageUrl,
        String authorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
