package org.bibliotecaviva.backend.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CommentSummary {
    UUID getId();

    String getContent();

    String getUserName();

    String getWorkTitle();

    LocalDateTime getCreatedAt();
}
