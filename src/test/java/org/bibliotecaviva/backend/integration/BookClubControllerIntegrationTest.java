package org.bibliotecaviva.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.bibliotecaviva.backend.domain.entities.BookClub;
import org.bibliotecaviva.backend.domain.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookClubControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void participantsShouldBePublicAndExposeOrganizerAndStudents() throws Exception {
        User organizer = createActiveCurator();
        User student = createActiveStudent();
        BookClub club = createBookClubInDatabase(organizer, futureDate(2));
        club.getParticipants().add(student);
        bookClubRepository.saveAndFlush(club);

        mockMvc.perform(get("/bookclub/{id}/participants", club.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizer").value(organizer.getName()))
                .andExpect(jsonPath("$.students[0]").value(student.getName()));
    }

    @Test
    void shouldCreateListGetNextUpdateSubscribeUnsubscribeAndDeleteBookClub() throws Exception {
        User curator = createActiveCurator();
        User student = createActiveStudent();
        LocalDateTime date = futureDate(2);
        Map<String, Object> payload = bookClubPayload("Dom Casmurro", date);

        JsonNode createResponse = jsonFrom(mockMvc.perform(post("/bookclub")
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookName").value("Dom Casmurro"))
                .andExpect(jsonPath("$.organizerName").value(curator.getName()))
                .andExpect(jsonPath("$.location").value("Biblioteca Municipal"))
                .andExpect(jsonPath("$.participantsCount").value(0))
                .andExpect(jsonPath("$.bookCoverUrl").value("https://example.com/capa.jpg"))
                .andReturn());
        UUID id = UUID.fromString(createResponse.get("id").asText());

        mockMvc.perform(get("/bookclub/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.bookName").value("Dom Casmurro"))
                .andExpect(jsonPath("$.participantsCount").value(0));

        mockMvc.perform(get("/bookclub/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.bookName").value("Dom Casmurro"));

        mockMvc.perform(get("/bookclub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.content[0].bookName").value("Dom Casmurro"));

        Map<String, Object> updatePayload = bookClubPayload("A Hora da Estrela", futureDate(3));
        mockMvc.perform(put("/bookclub/{id}", id)
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.bookName").value("A Hora da Estrela"));

        mockMvc.perform(post("/bookclub/{id}/subscribe", id)
                        .header("Authorization", bearer(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Presen\u00e7a confirmada com sucesso"));

        mockMvc.perform(get("/bookclub/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantsCount").value(1));

        mockMvc.perform(post("/bookclub/{id}/subscribe", id)
                        .header("Authorization", bearer(student)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mockMvc.perform(post("/bookclub/{id}/unsubscribe", id)
                        .header("Authorization", bearer(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Presen\u00e7a cancelada com sucesso"));

        mockMvc.perform(get("/bookclub/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantsCount").value(0));

        mockMvc.perform(delete("/bookclub/{id}", id)
                        .header("Authorization", bearer(curator)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/bookclub/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createShouldReturnConflictWhenMonthAlreadyHasBookClub() throws Exception {
        User curator = createActiveCurator();
        LocalDateTime date = futureDate(4);
        createBookClubInDatabase(curator, date.withDayOfMonth(5));

        mockMvc.perform(post("/bookclub")
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(bookClubPayload("Outro livro", date.withDayOfMonth(20)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createShouldReturnBadRequestForInvalidPayload() throws Exception {
        User curator = createActiveCurator();
        Map<String, Object> payload = bookClubPayload("Do", futureDate(5));
        payload.put("bookCoverUrl", "url-invalida");

        mockMvc.perform(post("/bookclub")
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidFields").isArray());
    }

    @Test
    void subscribeShouldReturnConflictWhenParticipantLimitIsReached() throws Exception {
        User curator = createActiveCurator();
        BookClub bookClub = createBookClubInDatabase(curator, futureDate(6));
        for (int i = 0; i < 25; i++) {
            bookClub.getParticipants().add(createActiveStudent());
        }
        bookClubRepository.saveAndFlush(bookClub);
        User extraStudent = createActiveStudent();

        mockMvc.perform(post("/bookclub/{id}/subscribe", bookClub.getId())
                        .header("Authorization", bearer(extraStudent)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void anonymousUserShouldNotCreateBookClub() throws Exception {
        mockMvc.perform(post("/bookclub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(bookClubPayload("Dom Casmurro", futureDate(7)))))
                .andExpect(status().isForbidden());
    }

    private Map<String, Object> bookClubPayload(String bookName, LocalDateTime date) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("bookName", bookName);
        payload.put("bookSynopses", "Sinopse valida para o clube do livro de teste");
        payload.put("bookAuthor", "Machado de Assis");
        payload.put("date", date.toString());
        payload.put("location", "Biblioteca Municipal");
        payload.put("bookCoverUrl", "https://example.com/capa.jpg");
        return payload;
    }

    private LocalDateTime futureDate(int monthsFromNow) {
        return LocalDateTime.now()
                .plusMonths(monthsFromNow)
                .withDayOfMonth(10)
                .withHour(19)
                .withMinute(30)
                .withSecond(0)
                .withNano(0);
    }
}
