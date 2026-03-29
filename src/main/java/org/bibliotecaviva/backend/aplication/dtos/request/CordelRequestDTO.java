package org.bibliotecaviva.backend.aplication.dtos.request;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.bibliotecaviva.backend.domain.entities.textual.Cordel}
 */
public record CordelRequestDTO(String title, String author, LocalDateTime publicationDate, String description,
                               String content, String rhymeScheme) implements Serializable {
}