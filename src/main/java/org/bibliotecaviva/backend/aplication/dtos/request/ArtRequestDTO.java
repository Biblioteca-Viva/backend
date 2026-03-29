package org.bibliotecaviva.backend.aplication.dtos.request;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.bibliotecaviva.backend.domain.entities.visual.Art}
 */
public record ArtRequestDTO(String title, String author, LocalDateTime publicationDate, String description, String url,
                            Duration duration) implements Serializable {
}