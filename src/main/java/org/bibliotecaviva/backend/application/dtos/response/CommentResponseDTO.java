package org.bibliotecaviva.backend.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentResponseDTO(
        UUID id,
        String content,
        String authorName,
        LocalDateTime createdAt,
        Long likes,
        CommentReplyResponseDTO reply
) {
}
