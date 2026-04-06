package org.bibliotecaviva.backend.application.dtos.response.audiovisual;

import io.swagger.v3.oas.annotations.media.Schema;
import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record LibraLiteratureResponseDTO(
        UUID id,
        String title,
        String author,
        LocalDateTime publicationDate,
        String description,
        String type,
        String url
) implements WorkResponse {
}
