package org.bibliotecaviva.backend.application.dtos.request.audiovisual;

import org.bibliotecaviva.backend.application.dtos.request.WorkRequest;

import java.time.LocalDateTime;

/**
 * DTO for {@link org.bibliotecaviva.backend.domain.entities.audiovisual.Multimedia}
 */
public record MultimediaRequestDTO(String title, String author, LocalDateTime publicationDate, String description,
                                   String url) implements WorkRequest {
}