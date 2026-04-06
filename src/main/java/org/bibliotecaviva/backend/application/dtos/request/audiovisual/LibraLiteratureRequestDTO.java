package org.bibliotecaviva.backend.application.dtos.request.audiovisual;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.bibliotecaviva.backend.application.dtos.request.WorkRequest;
import org.bibliotecaviva.backend.domain.entities.audiovisual.LibraLiterature;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * DTO for {@link LibraLiterature}
 */
public record LibraLiteratureRequestDTO(
        @NotBlank @Size(min = 3,max = 255, message = "Title must be between 3 and 255 characters")
        String title,
        @NotBlank
        String author,
        @PastOrPresent
        LocalDateTime publicationDate,
        @NotBlank @Size(min = 15,message = "Description must be at least 15 characters long")
        String description,
        @URL @NotBlank //Colocar um pattern no url para dominios especificos
        String url
) implements WorkRequest {
}