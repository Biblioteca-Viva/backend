package org.bibliotecaviva.backend.application.dtos.request;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.bibliotecaviva.backend.domain.entities.BookClub}
 */
public record BookClubRequestDTO(
        @NotBlank(message = "Book name cannot be blank")
        @Size(min = 3 ,max = 255, message = "Book name must be between 3 and 255 characters")
        String bookName,
        @NotBlank(message = "Book synopses cannot be blank")
        @Size(min = 10, max = 1000, message = "Synopses must be between 10 and 1000 characters")
        String bookSynopses,
        @NotBlank(message = "Book author cannot be blank")
        @Size(min = 3, max = 255, message = "Author name must be between 3 and 255 characters")
        String bookAuthor,
        @Future(message = "Date must be in the future")@NotNull(message = "Date cannot be null")
        LocalDateTime date,
        @NotBlank(message = "Location cannot be blank")
        @Size(min = 3, max = 255, message = "Location must be between 3 and 255 characters")
        String location
){
}