package org.bibliotecaviva.backend.application.dtos.request.audiovisual;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.bibliotecaviva.backend.application.dtos.request.WorkRequest;
import org.bibliotecaviva.backend.domain.entities.audiovisual.LibraLiterature;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DurationFormat;

import java.time.LocalDateTime;

/**
 * DTO for {@link LibraLiterature}
 */
public record LibraLiteratureRequestDTO(
                @NotBlank(message = "Title cannot be blank") @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters") String title,
                @Email(message = "Author must be a valid email address(can be changed later)") @NotBlank(message = "Author email cannot be blank") String author,
                @NotNull(message = "Data cannot be empty") @PastOrPresent(message = "Publication date cannot be in the future") LocalDateTime publicationDate,
                @NotBlank(message = "description cannot be blank") @Size(min = 15, message = "Description must be at least 15 characters long") String description,
                @URL @NotBlank // Colocar um pattern no url para dominios especificos
                String url,
                @NotBlank @DurationFormat(style = DurationFormat.Style.ISO8601) @Schema(example = "PT3M30S or PT5M or PT45S", description = "Duration in ISO 8601 format)") String duration

) implements WorkRequest {
}