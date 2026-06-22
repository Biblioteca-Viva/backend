package org.bibliotecaviva.backend.application.dtos.response;

import java.util.List;

public record BookClubParticipants(
        String organizer,
        List<String> students
) {
}
