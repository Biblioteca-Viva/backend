package org.bibliotecaviva.backend.aplication.dtos.request;
import org.bibliotecaviva.backend.domain.entities.audiovisual.LibraLiterature;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link LibraLiterature}
 */
public record LibraLiteratureRequestDTO(String title, String author, LocalDateTime publicationDate, String description,
                                        String url) implements Serializable {
}