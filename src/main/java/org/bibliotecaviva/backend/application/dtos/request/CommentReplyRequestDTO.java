package org.bibliotecaviva.backend.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentReplyRequestDTO(
        @NotBlank @Size(max = 200) String content
) {}