package org.bibliotecaviva.backend.domain.entities.projections;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CommentSummary {
    UUID getId();

    String getContent();

    String getUserName();

    String getUserId();

    String getWorkTitle();

    String getWorkId();

    LocalDateTime getCreatedAt();

        UUID getReplyId();
        String getReplyContent();
        String getReplyAuthor();
        LocalDateTime getReplyCreatedAt();
}
