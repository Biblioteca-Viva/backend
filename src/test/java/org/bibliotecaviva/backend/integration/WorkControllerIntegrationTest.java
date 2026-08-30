package org.bibliotecaviva.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.bibliotecaviva.backend.domain.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkControllerIntegrationTest extends IntegrationTestSupport {

    @ParameterizedTest(name = "{0}")
    @MethodSource("workCases")
    void shouldRunFullCrudCycleForEveryWorkType(WorkEndpointCase spec) throws Exception {
        User curator = createActiveCurator();
        String authorization = bearer(curator);
        String title = uniqueTitle(spec.type());
        Map<String, Object> createPayload = spec.createPayload(baseWorkPayload(title, curator.getEmail()));

        ResultActions createResult = mockMvc.perform(post("/work/" + spec.path())
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.author").value(curator.getName()))
                .andExpect(jsonPath("$.type").value(spec.type()))
                .andExpect(jsonPath("$.viewCount").value(0))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.commentCount").value(0));
        assertSpecificFields(createResult, spec.createAssertions());

        UUID id = UUID.fromString(jsonFrom(createResult.andReturn()).get("id").asText());

        mockMvc.perform(get("/work/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.type").value(spec.type()))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.commentCount").value(0));

        flushAndClear();
        assertEquals(1L, workRepository.findById(id).orElseThrow().getViewCount());

        mockMvc.perform(get("/work")
                        .queryParam("type", spec.queryType()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.content[0].title").value(title))
                .andExpect(jsonPath("$.content[0].type").value(spec.type()));

        String updatedTitle = uniqueTitle(spec.type() + " atualizado");
        Map<String, Object> updatePayload = spec.updatePayload(baseWorkPayload(updatedTitle, curator.getEmail()));
        ResultActions updateResult = mockMvc.perform(put("/work/" + spec.path() + "/" + id)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value(updatedTitle))
                .andExpect(jsonPath("$.author").value(curator.getName()))
                .andExpect(jsonPath("$.type").value(spec.type()));
        assertSpecificFields(updateResult, spec.updateAssertions());

        mockMvc.perform(delete("/work/" + id)
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/work/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentShouldNotCreateWork() throws Exception {
        User student = createActiveStudent();
        User author = createActiveCurator();

        mockMvc.perform(post("/work/articles")
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(articlePayload(uniqueTitle("Artigo"), author.getEmail()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUserShouldNotCreateWork() throws Exception {
        User author = createActiveCurator();

        mockMvc.perform(post("/work/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(articlePayload(uniqueTitle("Artigo"), author.getEmail()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createShouldReturnNotFoundWhenAuthorDoesNotExist() throws Exception {
        User curator = createActiveCurator();

        mockMvc.perform(post("/work/articles")
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(articlePayload(uniqueTitle("Artigo"), uniqueEmail("sem-autor")))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createShouldReturnConflictWhenAuthorAlreadyHasWorkWithSameTitle() throws Exception {
        User curator = createActiveCurator();
        String authorization = bearer(curator);
        String title = uniqueTitle("Artigo duplicado");
        Map<String, Object> payload = articlePayload(title, curator.getEmail());

        mockMvc.perform(post("/work/articles")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/work/articles")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createShouldReturnBadRequestForInvalidPayload() throws Exception {
        User curator = createActiveCurator();
        Map<String, Object> invalidPayload = articlePayload("ab", curator.getEmail());
        invalidPayload.put("content", "");

        mockMvc.perform(post("/work/articles")
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalidPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidFields").isArray());
    }

    @Test
    void cordelShouldReturnLinkedIllustrationAndRejectUnknownArt() throws Exception {
        User curator = createActiveCurator();
        String authorization = bearer(curator);
        String firstArtTitle = uniqueTitle("First illustration");
        UUID firstArtId = createWorkThroughApi("arts", artPayload(firstArtTitle, curator.getEmail()), authorization);
        String cordelTitle = uniqueTitle("Illustrated cordel");
        Map<String, Object> cordel = baseWorkPayload(cordelTitle, curator.getEmail());
        cordel.put("content", "Cordel content");
        cordel.put("rhymeScheme", "ABAB");
        cordel.put("artName", firstArtTitle);

        JsonNode created = jsonFrom(mockMvc.perform(post("/work/cordels")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cordel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.illustration.id").value(firstArtId.toString()))
                .andExpect(jsonPath("$.illustration.title").value(firstArtTitle))
                .andExpect(jsonPath("$.illustration.url").value("https://example.com/arte.png"))
                .andReturn());
        UUID cordelId = UUID.fromString(created.get("id").asText());

        String secondArtTitle = uniqueTitle("Second illustration");
        UUID secondArtId = createWorkThroughApi("arts", artPayload(secondArtTitle, curator.getEmail()), authorization);
        cordel.put("title", uniqueTitle("Updated cordel"));
        cordel.put("artName", secondArtTitle);
        mockMvc.perform(put("/work/cordels/{id}", cordelId)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cordel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.illustration.id").value(secondArtId.toString()))
                .andExpect(jsonPath("$.illustration.title").value(secondArtTitle));

        Map<String, Object> invalid = new LinkedHashMap<>(cordel);
        invalid.put("title", uniqueTitle("Invalid illustrated cordel"));
        invalid.put("artName", uniqueTitle("Missing art"));
        mockMvc.perform(post("/work/cordels")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherShouldBeCreatedWithoutLinkAndImage() throws Exception {
        User curator = createActiveCurator();
        Map<String, Object> payload = baseWorkPayload(uniqueTitle("Obra geral"), curator.getEmail());
        payload.put("content", "Conteudo geral");

        mockMvc.perform(post("/work/others")
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("Other"))
                .andExpect(jsonPath("$.content").value("Conteudo geral"))
                .andExpect(jsonPath("$.url").isEmpty())
                .andExpect(jsonPath("$.imageUrl").isEmpty());
    }

    @Test
    void otherShouldAcceptEmptyLinkAndImage() throws Exception {
        User curator = createActiveCurator();
        Map<String, Object> payload = baseWorkPayload(uniqueTitle("Obra geral"), curator.getEmail());
        payload.put("content", "Conteudo geral");
        payload.put("url", "");
        payload.put("imageUrl", "");

        mockMvc.perform(post("/work/others")
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").isEmpty())
                .andExpect(jsonPath("$.imageUrl").isEmpty());
    }

    @Test
    void otherShouldRejectInvalidLinkAndImage() throws Exception {
        User curator = createActiveCurator();
        Map<String, Object> payload = baseWorkPayload(uniqueTitle("Obra geral"), curator.getEmail());
        payload.put("content", "Conteudo geral");
        payload.put("url", "nao-e-uma-url");
        payload.put("imageUrl", "tambem-nao-e");

        mockMvc.perform(post("/work/others")
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.invalidFields").isArray());
    }

    @Test
    void homeShouldAggregateCountsAndHighlights() throws Exception {
        User curator = createActiveCurator();
        String authorization = bearer(curator);

        createWorkThroughApi("articles", articlePayload(uniqueTitle("Artigo home"), curator.getEmail()), authorization);
        createWorkThroughApi("arts", artPayload(uniqueTitle("Arte home"), curator.getEmail()), authorization);

        JsonNode response = jsonFrom(mockMvc.perform(get("/work/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleCount").value(1))
                .andExpect(jsonPath("$.artCount").value(1))
                .andExpect(jsonPath("$.works").isArray())
                .andExpect(jsonPath("$.mostLikedWorks").isArray())
                .andReturn());

        assertTrue(response.get("works").size() >= 2);
        assertTrue(response.get("mostLikedWorks").size() >= 1);
    }

    private UUID createWorkThroughApi(String path, Map<String, Object> payload, String authorization) throws Exception {
        JsonNode response = jsonFrom(mockMvc.perform(post("/work/" + path)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isCreated())
                .andReturn());
        return UUID.fromString(response.get("id").asText());
    }

    private void assertSpecificFields(ResultActions result, Map<String, Object> expectedFields) throws Exception {
        for (Map.Entry<String, Object> field : expectedFields.entrySet()) {
            result.andExpect(jsonPath("$." + field.getKey()).value(field.getValue()));
        }
    }

    private Map<String, Object> articlePayload(String title, String authorEmail) {
        Map<String, Object> payload = baseWorkPayload(title, authorEmail);
        payload.put("content", "Conteudo do artigo");
        return payload;
    }

    private Map<String, Object> artPayload(String title, String authorEmail) {
        Map<String, Object> payload = baseWorkPayload(title, authorEmail);
        payload.put("url", "https://example.com/arte.png");
        return payload;
    }

    private static Stream<Arguments> workCases() {
        return Stream.of(
                Arguments.of(new WorkEndpointCase(
                        "poems",
                        "POEM",
                        "Poem",
                        p -> {
                            p.put("content", "Poem content");
                            p.put("rhymeScheme", "AABB");
                            p.put("poemType", "Sonnet");
                        },
                        p -> {
                            p.put("content", "Updated poem content");
                            p.put("rhymeScheme", "ABAB");
                            p.put("poemType", "Free verse");
                        },
                        Map.of("content", "Poem content"),
                        Map.of("content", "Updated poem content")
                )),
                Arguments.of(new WorkEndpointCase(
                        "articles",
                        "ARTICLE",
                        "Article",
                        p -> p.put("content", "Conteudo do artigo"),
                        p -> p.put("content", "Conteudo do artigo atualizado"),
                        Map.of("content", "Conteudo do artigo"),
                        Map.of("content", "Conteudo do artigo atualizado")
                )),
                Arguments.of(new WorkEndpointCase(
                        "others",
                        "OTHER",
                        "Other",
                        p -> {
                            p.put("content", "Conteudo geral");
                            p.put("url", "https://example.com/material.pdf");
                            p.put("imageUrl", "https://example.com/capa.png");
                        },
                        p -> {
                            p.put("content", "Conteudo geral atualizado");
                            p.put("url", "https://example.com/material-novo.pdf");
                            p.put("imageUrl", "https://example.com/capa-nova.png");
                        },
                        orderedMap("content", "Conteudo geral", "url", "https://example.com/material.pdf",
                                "imageUrl", "https://example.com/capa.png"),
                        orderedMap("content", "Conteudo geral atualizado", "url", "https://example.com/material-novo.pdf",
                                "imageUrl", "https://example.com/capa-nova.png")
                )),
                Arguments.of(new WorkEndpointCase(
                        "cordels",
                        "CORDEL",
                        "Cordel",
                        p -> {
                            p.put("content", "Conteudo do cordel");
                            p.put("rhymeScheme", "ABAB");
                        },
                        p -> {
                            p.put("content", "Conteudo do cordel atualizado");
                            p.put("rhymeScheme", "AABB");
                        },
                        orderedMap("content", "Conteudo do cordel", "rhymeScheme", "ABAB"),
                        orderedMap("content", "Conteudo do cordel atualizado", "rhymeScheme", "AABB")
                )),
                Arguments.of(new WorkEndpointCase(
                        "essays",
                        "ESSAY",
                        "Essay",
                        p -> {
                            p.put("content", "Conteudo da redacao");
                            p.put("rate", 980);
                            p.put("theme", "Tema da redacao");
                            p.put("themeDescription", "Descricao do tema");
                            p.put("feedback", "Excelente desenvolvimento");
                        },
                        p -> {
                            p.put("content", "Conteudo da redacao atualizado");
                            p.put("rate", 1000);
                            p.put("theme", "Tema atualizado");
                            p.put("themeDescription", "Descricao atualizada");
                            p.put("feedback", "Feedback atualizado");
                        },
                        orderedMap("content", "Conteudo da redacao", "rate", 980, "theme", "Tema da redacao",
                                "themeDescription", "Descricao do tema", "feedback", "Excelente desenvolvimento"),
                        orderedMap("content", "Conteudo da redacao atualizado", "rate", 1000, "theme", "Tema atualizado",
                                "themeDescription", "Descricao atualizada", "feedback", "Feedback atualizado")
                )),
                Arguments.of(new WorkEndpointCase(
                        "short-stories",
                        "SHORT_STORY",
                        "ShortStory",
                        p -> p.put("content", "Conteudo do conto curto"),
                        p -> p.put("content", "Conteudo do conto curto atualizado"),
                        Map.of("content", "Conteudo do conto curto"),
                        Map.of("content", "Conteudo do conto curto atualizado")
                )),
                Arguments.of(new WorkEndpointCase(
                        "tales",
                        "TALE",
                        "Tale",
                        p -> {
                            p.put("content", "Conteudo do conto");
                            p.put("genre", "Fantasia");
                        },
                        p -> {
                            p.put("content", "Conteudo do conto atualizado");
                            p.put("genre", "Suspense");
                        },
                        orderedMap("content", "Conteudo do conto", "genre", "Fantasia"),
                        orderedMap("content", "Conteudo do conto atualizado", "genre", "Suspense")
                )),
                Arguments.of(new WorkEndpointCase(
                        "arts",
                        "ART",
                        "Art",
                        p -> p.put("url", "https://example.com/art.png"),
                        p -> p.put("url", "https://example.com/art-updated.png"),
                        Map.of("url", "https://example.com/art.png"),
                        Map.of("url", "https://example.com/art-updated.png")
                )),
                Arguments.of(new WorkEndpointCase(
                        "infographics",
                        "INFOGRAPHIC",
                        "Infographic",
                        p -> p.put("url", "https://example.com/info.png"),
                        p -> p.put("url", "https://example.com/info-updated.png"),
                        Map.of("url", "https://example.com/info.png"),
                        Map.of("url", "https://example.com/info-updated.png")
                )),
                Arguments.of(new WorkEndpointCase(
                        "multimedias",
                        "MULTIMEDIA",
                        "Multimedia",
                        p -> {
                            p.put("url", "https://example.com/video.mp4");
                            p.put("duration", "PT3M30S");
                        },
                        p -> {
                            p.put("url", "https://example.com/video-updated.mp4");
                            p.put("duration", "PT4M");
                        },
                        orderedMap("url", "https://example.com/video.mp4", "duration", "PT3M30S"),
                        orderedMap("url", "https://example.com/video-updated.mp4", "duration", "PT4M")
                )),
                Arguments.of(new WorkEndpointCase(
                        "libra-literatures",
                        "LIBRA_LITERATURE",
                        "LibraLiterature",
                        p -> {
                            p.put("url", "https://example.com/libras.mp4");
                            p.put("duration", "PT3M30S");
                        },
                        p -> {
                            p.put("url", "https://example.com/libras-updated.mp4");
                            p.put("duration", "PT4M");
                        },
                        orderedMap("url", "https://example.com/libras.mp4", "duration", "PT3M30S"),
                        orderedMap("url", "https://example.com/libras-updated.mp4", "duration", "PT4M")
                ))
        );
    }

    private static Map<String, Object> orderedMap(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private record WorkEndpointCase(
            String path,
            String queryType,
            String type,
            Consumer<Map<String, Object>> createFields,
            Consumer<Map<String, Object>> updateFields,
            Map<String, Object> createAssertions,
            Map<String, Object> updateAssertions
    ) {
        Map<String, Object> createPayload(Map<String, Object> basePayload) {
            createFields.accept(basePayload);
            return basePayload;
        }

        Map<String, Object> updatePayload(Map<String, Object> basePayload) {
            updateFields.accept(basePayload);
            return basePayload;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}
