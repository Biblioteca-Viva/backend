package org.bibliotecaviva.backend.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
    public abstract class VisualWorkResponseDTO extends WorkResponseDTO {
    private String url;
}
