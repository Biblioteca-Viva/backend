package org.bibliotecaviva.backend.aplication.dtos.request;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.bibliotecaviva.backend.domain.entities.audiovisual.Multimedia}
 */
public record MultimediaRequestDTO(String title, String author, LocalDateTime publicationDate, String description,
                                   String url) implements Serializable {
}