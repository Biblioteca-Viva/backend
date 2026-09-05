package org.bibliotecaviva.backend.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for {@link org.bibliotecaviva.backend.domain.entities.News}
 */
public record NewsRequestDTO(
        @NotBlank(message = "O título não pode estar em branco")
        @Size(min = 3, max = 255, message = "O título deve ter entre 3 e 255 caracteres")
        String title,

        @NotBlank(message = "O conteúdo não pode estar em branco")
        @Size(min = 10, message = "O conteúdo deve ter no mínimo 10 caracteres")
        String content
) {
}
