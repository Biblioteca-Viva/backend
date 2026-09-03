package org.bibliotecaviva.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.bibliotecaviva.backend.application.services.CloudinaryService;
import org.bibliotecaviva.backend.domain.entities.News;
import org.bibliotecaviva.backend.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NewsControllerIntegrationTest extends IntegrationTestSupport {

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setupCloudinary() {
        when(cloudinaryService.uploadImage(any()))
                .thenReturn("https://res.cloudinary.com/test/news.jpg");
    }

    // ────────────────────────── create ──────────────────────────────

    @Test
    void shouldCreateNewsWithoutImage() throws Exception {
        User curator = createActiveCurator();

        JsonNode response = jsonFrom(mockMvc.perform(multipart("/news")
                        .file(dataPart(newsPayload("Feira do Livro 2026", "Conteúdo da notícia longo o suficiente")))
                        .header("Authorization", bearer(curator)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Feira do Livro 2026"))
                .andExpect(jsonPath("$.content").value("Conteúdo da notícia longo o suficiente"))
                .andExpect(jsonPath("$.authorName").value(curator.getName()))
                .andExpect(jsonPath("$.imageUrl").isEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn());

        UUID id = UUID.fromString(response.get("id").asText());
        mockMvc.perform(get("/news/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void shouldCreateNewsWithImage() throws Exception {
        User admin = createActiveAdmin();

        JsonNode response = jsonFrom(mockMvc.perform(multipart("/news")
                        .file(dataPart(newsPayload("Notícia com Foto", "Conteúdo da notícia com imagem")))
                        .file(imagePart())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Notícia com Foto"))
                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/test/news.jpg"))
                .andReturn());

        UUID id = UUID.fromString(response.get("id").asText());
        mockMvc.perform(get("/news/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/test/news.jpg"));
    }

    @Test
    void createShouldReturnBadRequestForInvalidPayload() throws Exception {
        User curator = createActiveCurator();
        Map<String, Object> invalid = new LinkedHashMap<>();
        invalid.put("title", "Ab");         // muito curto — mínimo 3
        invalid.put("content", "Curto");    // muito curto — mínimo 10

        mockMvc.perform(multipart("/news")
                        .file(dataPart(invalid))
                        .header("Authorization", bearer(curator)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidFields").isArray());
    }

    @Test
    void createShouldReturnForbiddenForAnonymousUser() throws Exception {
        mockMvc.perform(multipart("/news")
                        .file(dataPart(newsPayload("Notícia anônima", "Conteúdo qualquer longo o suficiente"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createShouldReturnForbiddenForStudentRole() throws Exception {
        User student = createActiveStudent();

        mockMvc.perform(multipart("/news")
                        .file(dataPart(newsPayload("Título proibido", "Conteúdo que aluno não pode criar")))
                        .header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());
    }

    // ────────────────────────── getAll ──────────────────────────────

    @Test
    void getAllShouldBePublicAndReturnPage() throws Exception {
        User curator = createActiveCurator();
        createNewsInDatabase(curator, "Notícia A");
        createNewsInDatabase(curator, "Notícia B");

        mockMvc.perform(get("/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void getAllShouldReturnEmptyPageWhenNoNews() throws Exception {
        mockMvc.perform(get("/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    // ────────────────────────── getById ──────────────────────────────

    @Test
    void getByIdShouldBePublicAndReturnNewsDetails() throws Exception {
        User curator = createActiveCurator();
        News news = createNewsInDatabase(curator, "Notícia Pública");

        mockMvc.perform(get("/news/{id}", news.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(news.getId().toString()))
                .andExpect(jsonPath("$.title").value("Notícia Pública"))
                .andExpect(jsonPath("$.authorName").value(curator.getName()));
    }

    @Test
    void getByIdShouldReturnNotFoundForNonExistentNews() throws Exception {
        mockMvc.perform(get("/news/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ────────────────────────── update ──────────────────────────────

    @Test
    void updateShouldAllowOwnerCuradorToEditOwnNews() throws Exception {
        User curator = createActiveCurator();
        News news = createNewsInDatabase(curator, "Título original");

        mockMvc.perform(multipart(HttpMethod.PUT, "/news/{id}", news.getId())
                        .file(dataPart(newsPayload("Título atualizado", "Conteúdo atualizado longo o suficiente")))
                        .header("Authorization", bearer(curator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Título atualizado"))
                .andExpect(jsonPath("$.content").value("Conteúdo atualizado longo o suficiente"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void updateShouldAllowAdminToEditAnyNews() throws Exception {
        User curator = createActiveCurator();
        User admin = createActiveAdmin();
        News news = createNewsInDatabase(curator, "Notícia do curador");

        mockMvc.perform(multipart(HttpMethod.PUT, "/news/{id}", news.getId())
                        .file(dataPart(newsPayload("Corrigida pelo admin", "Conteúdo corrigido suficientemente longo")))
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Corrigida pelo admin"));
    }

    @Test
    void updateShouldAddImageWhenPreviouslyWithoutOne() throws Exception {
        User curator = createActiveCurator();
        News news = createNewsInDatabase(curator, "Sem foto inicial");

        mockMvc.perform(multipart(HttpMethod.PUT, "/news/{id}", news.getId())
                        .file(dataPart(newsPayload("Com foto agora", "Conteúdo com foto suficientemente longo")))
                        .file(imagePart())
                        .header("Authorization", bearer(curator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/test/news.jpg"));
    }

    @Test
    void updateShouldReturnForbiddenWhenCuradorIsNotOwner() throws Exception {
        User originalAuthor = createActiveCurator();
        User otherCurador = createActiveCurator();
        News news = createNewsInDatabase(originalAuthor, "Notícia do outro");

        mockMvc.perform(multipart(HttpMethod.PUT, "/news/{id}", news.getId())
                        .file(dataPart(newsPayload("Tentativa indevida", "Conteúdo que não é permitido editar")))
                        .header("Authorization", bearer(otherCurador)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateShouldReturnNotFoundForNonExistentNews() throws Exception {
        User curator = createActiveCurator();

        mockMvc.perform(multipart(HttpMethod.PUT, "/news/{id}", UUID.randomUUID())
                        .file(dataPart(newsPayload("Não existe", "Conteúdo de notícia que não existe no banco")))
                        .header("Authorization", bearer(curator)))
                .andExpect(status().isNotFound());
    }

    // ────────────────────────── delete ──────────────────────────────

    @Test
    void deleteShouldAllowOwnerToDeleteOwnNews() throws Exception {
        User curator = createActiveCurator();
        News news = createNewsInDatabase(curator, "Notícia a deletar");

        mockMvc.perform(delete("/news/{id}", news.getId())
                        .header("Authorization", bearer(curator)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/news/{id}", news.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldAllowAdminToDeleteAnyNews() throws Exception {
        User curator = createActiveCurator();
        User admin = createActiveAdmin();
        News news = createNewsInDatabase(curator, "Deletada pelo admin");

        mockMvc.perform(delete("/news/{id}", news.getId())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteShouldReturnForbiddenWhenCuradorIsNotOwner() throws Exception {
        User originalAuthor = createActiveCurator();
        User otherCurador = createActiveCurator();
        News news = createNewsInDatabase(originalAuthor, "Não pode deletar");

        mockMvc.perform(delete("/news/{id}", news.getId())
                        .header("Authorization", bearer(otherCurador)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteShouldReturnForbiddenForAnonymousUser() throws Exception {
        User curator = createActiveCurator();
        News news = createNewsInDatabase(curator, "Notícia protegida");

        mockMvc.perform(delete("/news/{id}", news.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteShouldReturnNotFoundForNonExistentNews() throws Exception {
        User admin = createActiveAdmin();

        mockMvc.perform(delete("/news/{id}", UUID.randomUUID())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    // ────────────────────────── helpers ──────────────────────────────

    protected News createNewsInDatabase(User author, String title) {
        News news = News.builder()
                .title(title)
                .content("Conteúdo da notícia de teste para validação da integração")
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();
        return newsRepository.saveAndFlush(news);
    }

    private MockMultipartFile dataPart(Map<String, Object> payload) throws Exception {
        return new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE,
                json(payload).getBytes());
    }

    private MockMultipartFile imagePart() {
        return new MockMultipartFile("image", "news.jpg", "image/jpeg",
                "fake-image-content".getBytes());
    }

    private Map<String, Object> newsPayload(String title, String content) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("content", content);
        return payload;
    }
}
