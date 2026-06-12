package org.bibliotecaviva.backend.application.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentSummaryResponseDTO(
        UUID id,
        String content,
        String userName,
        String userId,
        String workTitle,
        String workId,
        LocalDateTime createdAt,
        UUID replyId,
        String replyContent,
        String replyAuthor,
        LocalDateTime replyCreatedAt
) {
}
