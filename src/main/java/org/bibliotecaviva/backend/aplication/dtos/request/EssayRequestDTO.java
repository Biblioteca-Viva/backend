package org.bibliotecaviva.backend.aplication.dtos.request;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.bibliotecaviva.backend.domain.entities.textual.Essay}
 */
public record EssayRequestDTO(String title, String author, LocalDateTime publicationDate, String description,
                              String content, Integer rate, String theme, String themeDescription,
                              String feedback) implements Serializable {
}