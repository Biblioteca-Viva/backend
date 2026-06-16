package org.bibliotecaviva.backend.application.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentReplyResponseDTO(
        UUID id,
        String content,
        String authorName,
        LocalDateTime createdAt
) {}