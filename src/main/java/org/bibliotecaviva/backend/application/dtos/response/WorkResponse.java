package org.bibliotecaviva.backend.application.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.bibliotecaviva.backend.application.dtos.response.audiovisual.LibraLiteratureResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.audiovisual.MultimediaResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.textual.*;
import org.bibliotecaviva.backend.application.dtos.response.visual.ArtResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.visual.InfographicReponseDTO;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * marker interface for all works responses
 */
@Schema(name = "WorkResponse", description = "Interface for all work response DTOs", oneOf =
        { LibraLiteratureResponseDTO.class, MultimediaResponseDTO.class, ArticleResponseDTO.class,
                CordelResponseDTO.class, EssayResponseDTO.class, ShortStoryResponseDTO.class, TaleResponseDTO.class,
                ArtResponseDTO.class, InfographicReponseDTO.class}
)
public interface WorkResponse {
    UUID id();

    String title();

    String author();

    LocalDateTime publicationDate();

    String description();

    Long viewCount();

    Long likeCount();

    Long commentCount();
}
