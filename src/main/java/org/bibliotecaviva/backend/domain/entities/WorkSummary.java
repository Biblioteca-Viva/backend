package org.bibliotecaviva.backend.domain.entities;

import jakarta.persistence.Column;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// pra puxar do tabelao somente campos que serao apresentados na tela inicial
// somente resumo das obras( usado somente em workrepository)
public interface WorkSummary {
    UUID getId();
    String getTitle();
    String getAuthor();
    LocalDateTime getPublicationDate();
    String getDescription();
    String getType();
    Long getViewCount();
    Long getLikeCount();
}
