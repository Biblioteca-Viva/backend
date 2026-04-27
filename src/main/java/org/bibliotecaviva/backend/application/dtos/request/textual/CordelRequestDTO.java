package org.bibliotecaviva.backend.application.dtos.request.textual;

import jakarta.validation.constraints.*;
import org.bibliotecaviva.backend.application.dtos.request.WorkRequest;

import java.time.LocalDateTime;

/**
 * DTO for {@link org.bibliotecaviva.backend.domain.entities.textual.Cordel}
 */
public record CordelRequestDTO(
        @NotBlank(message = "Title cannot be blank") @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,
        @Email(message = "Author must be a valid email address(can be changed later)") @NotBlank(message = "Author email cannot be blank")
        String author,
        @NotNull(message = "Data cannot be empty") @PastOrPresent(message = "Publication date cannot be in the future")
        LocalDateTime publicationDate,
        @NotBlank(message = "description cannot be blank") @Size(min = 15, message = "Description must be at least 15 characters long")
        String description,
        @NotBlank(message = "Content can not be blank") //Veriicar dps se coloca minimo e maximo de caracteres
        String content,
        //TODO: ENUM PARA ESQUEMAS
        @NotBlank(message = "Rhyme scheme can not be blank")
        String rhymeScheme,
        String artName
        //botar auto tbm
) implements WorkRequest {
}