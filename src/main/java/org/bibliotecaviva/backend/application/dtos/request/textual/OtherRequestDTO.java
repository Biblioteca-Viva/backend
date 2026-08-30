package org.bibliotecaviva.backend.application.dtos.request.textual;

import jakarta.validation.constraints.*;
import org.bibliotecaviva.backend.application.dtos.request.WorkRequest;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * DTO for {@link org.bibliotecaviva.backend.domain.entities.textual.Other}
 */
public record OtherRequestDTO(
        @NotBlank(message = "Title cannot be blank") @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,
        @Email(message = "Author must be a valid email address") String authorEmail,
        @Size(min = 3, max = 255) String authorName,
        @NotNull(message = "Data cannot be empty") @PastOrPresent(message = "Publication date cannot be in the future")
        LocalDateTime publicationDate,
        @NotBlank(message = "description cannot be blank") @Size(min = 15, message = "Description must be at least 15 characters long")
        String description,
        @NotBlank(message = "Content can not be blank")
        String content,
        // link e imagem sao opcionais nessa categoria
        @URL(message = "URL must be a valid address")
        String url,
        @URL(message = "Image URL must be a valid address")
        String imageUrl,
        @NotBlank @Size(min = 3, max = 50, message = "Student class must be between 3 and 50 characters")
        String studentClass
) implements WorkRequest {
}
