package org.bibliotecaviva.backend.application.dtos.response.visual;

import java.util.UUID;

public record IllustrationResponseDTO(
        UUID id,
        String title,
        String url
) {
}
