package org.bibliotecaviva.backend.application.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.bibliotecaviva.backend.application.dtos.response.audiovisual.LibraLiteratureResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.audiovisual.MultimediaResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.textual.*;
import org.bibliotecaviva.backend.application.dtos.response.visual.ArtResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.visual.InfographicReponseDTO;

/*
 * interface marcadora
 */
@Schema(name = "WorkResponse", description = "Interface for all work response DTOs",oneOf =
        {WorkResponseDTO.class, LibraLiteratureResponseDTO.class, MultimediaResponseDTO.class, ArticleResponseDTO.class,
        CordelResponseDTO.class, EssayResponseDTO.class, ShortStoryResponseDTO.class, TaleResponseDTO.class,
        ArtResponseDTO.class, InfographicReponseDTO.class}
)
public interface WorkResponse {
}
