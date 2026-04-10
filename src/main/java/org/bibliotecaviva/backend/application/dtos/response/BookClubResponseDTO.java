package org.bibliotecaviva.backend.application.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookClubResponseDTO(
        UUID id,
        String organizerId,
        String bookName,
        String bookSynopses,
        String bookAuthor,
        LocalDateTime date,
        String location,
        Long participantsCount
) {

}
