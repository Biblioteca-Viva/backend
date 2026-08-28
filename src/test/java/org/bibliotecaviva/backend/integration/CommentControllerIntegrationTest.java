package org.bibliotecaviva.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.bibliotecaviva.backend.domain.entities.Comment;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.entities.textual.Article;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
class CommentControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void studentLinkedAsWorkAuthorShouldReplyWithoutForbiddenResponse() throws Exception {
        User studentAuthor = createActiveStudent();
        User commenter = createActiveStudent();
        Article work = createArticleInDatabase(studentAuthor);
        Comment comment = createCommentInDatabase(commenter, work, "Question for the author");

        mockMvc.perform(post("/work/{workId}/comments/{commentId}/reply", work.getId(), comment.getId())
                        .header("Authorization", bearer(studentAuthor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Student author response"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Student author response"))
                .andExpect(jsonPath("$.authorName").value(studentAuthor.getName()));
    }

    @Test
    void authenticatedUserShouldLikeAndUnlikeComment() throws Exception {
        User author = createActiveCurator();
        User commenter = createActiveStudent();
        User voter = createActiveStudent();
        Article work = createArticleInDatabase(author);
        Comment comment = createCommentInDatabase(commenter, work, "Comment to like");

        mockMvc.perform(put("/work/{workId}/comments/{commentId}/like", work.getId(), comment.getId())
                        .header("Authorization", bearer(voter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likeCount").value(1));
        mockMvc.perform(delete("/work/{workId}/comments/{commentId}/like", work.getId(), comment.getId())
                        .header("Authorization", bearer(voter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false))
                .andExpect(jsonPath("$.likeCount").value(0));
    }

    @Test
    void shouldCreateListUpdateDenyThirdPartyAndDeleteComment() throws Exception {
        User curator = createActiveCurator();
        User owner = createActiveStudent();
        User thirdParty = createActiveStudent();
        User admin = createActiveAdmin();
        Article work = createArticleInDatabase(curator);

        JsonNode createResponse = jsonFrom(mockMvc.perform(post("/work/{workId}/comments", work.getId())
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario inicial"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Comentario inicial"))
                .andExpect(jsonPath("$.authorName").value(owner.getName()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn());
        UUID commentId = UUID.fromString(createResponse.get("id").asText());

        mockMvc.perform(get("/work/{workId}/comments", work.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$.content[0].content").value("Comentario inicial"))
                .andExpect(jsonPath("$.content[0].authorName").value(owner.getName()));

        mockMvc.perform(put("/work/{workId}/comments/{commentId}", work.getId(), commentId)
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario atualizado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("Comentario atualizado"));

        mockMvc.perform(put("/work/{workId}/comments/{commentId}", work.getId(), commentId)
                        .header("Authorization", bearer(thirdParty))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Tentativa de terceiro"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(delete("/work/{workId}/comments/{commentId}", work.getId(), commentId)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/work/{workId}/comments/{commentId}", work.getId(), commentId)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createShouldReturnNotFoundWhenWorkDoesNotExist() throws Exception {
        User student = createActiveStudent();

        mockMvc.perform(post("/work/{workId}/comments", UUID.randomUUID())
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldCreateAndGetReply() throws Exception {
        User curator = createActiveCurator();
        User student = createActiveStudent();
        Article work = createArticleInDatabase(curator);

        // student cria comentário
        JsonNode createComment = jsonFrom(mockMvc.perform(post("/work/{workId}/comments", work.getId())
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario"))))
                .andExpect(status().isCreated())
                .andReturn());
        UUID commentId = UUID.fromString(createComment.get("id").asText());

        // curator (autor da obra) responde
        mockMvc.perform(post("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId)
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Resposta do autor"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Resposta do autor"))
                .andExpect(jsonPath("$.authorName").value(curator.getName()))
                .andExpect(jsonPath("$.id").isNotEmpty());

        // GET da reply pelo commentId
        mockMvc.perform(get("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Resposta do autor"))
                .andExpect(jsonPath("$.authorName").value(curator.getName()));
    }

    @Test
    void replyShouldFailWhenCommentAlreadyHasReply() throws Exception {
        User curator = createActiveCurator();
        User student = createActiveStudent();
        Article work = createArticleInDatabase(curator);

        JsonNode createComment = jsonFrom(mockMvc.perform(post("/work/{workId}/comments", work.getId())
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario"))))
                .andExpect(status().isCreated())
                .andReturn());
        UUID commentId = UUID.fromString(createComment.get("id").asText());

        // primeira reply — deve funcionar
        mockMvc.perform(post("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId)
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Primeira resposta"))))
                .andExpect(status().isCreated());

        // segunda reply no mesmo comentário — deve falhar
        mockMvc.perform(post("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId)
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Segunda resposta"))))
                .andExpect(status().isConflict());
    }

    @Test
    void replyShouldFailWhenUserIsNeitherAdminNorWorkAuthor() throws Exception {
        User curator = createActiveCurator();
        User student = createActiveStudent();
        User thirdParty = createActiveStudent();
        Article work = createArticleInDatabase(curator);

        JsonNode createComment = jsonFrom(mockMvc.perform(post("/work/{workId}/comments", work.getId())
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario"))))
                .andExpect(status().isCreated())
                .andReturn());
        UUID commentId = UUID.fromString(createComment.get("id").asText());

        mockMvc.perform(post("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId)
                        .header("Authorization", bearer(thirdParty))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Tentativa de terceiro"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void replyShouldFailWhenCommentDoesNotExist() throws Exception {
        User admin = createActiveAdmin();

        mockMvc.perform(post("/work/{workId}/comments/{commentId}/reply", UUID.randomUUID(), UUID.randomUUID())
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Resposta"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldUpdateAndDeleteReply() throws Exception {
        User curator = createActiveCurator();
        User admin = createActiveAdmin();
        User student = createActiveStudent();
        Article work = createArticleInDatabase(curator);

        JsonNode createComment = jsonFrom(mockMvc.perform(post("/work/{workId}/comments", work.getId())
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario"))))
                .andExpect(status().isCreated())
                .andReturn());
        UUID commentId = UUID.fromString(createComment.get("id").asText());

        JsonNode createReply = jsonFrom(mockMvc.perform(post("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId)
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Resposta original"))))
                .andExpect(status().isCreated())
                .andReturn());
        UUID replyId = UUID.fromString(createReply.get("id").asText());

        // curator atualiza a própria reply
        mockMvc.perform(put("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId)
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Resposta atualizada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Resposta atualizada"));

        // admin deleta a reply
        mockMvc.perform(delete("/work/{workId}/comments/{commentId}/reply/{replyId}", work.getId(), commentId, replyId)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        // GET após delete retorna 404
        mockMvc.perform(get("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReplyShouldFailWhenUserIsNeitherOwnerNorAdmin() throws Exception {
        User curator = createActiveCurator();
        User student = createActiveStudent();
        User thirdParty = createActiveStudent();
        Article work = createArticleInDatabase(curator);

        JsonNode createComment = jsonFrom(mockMvc.perform(post("/work/{workId}/comments", work.getId())
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario"))))
                .andExpect(status().isCreated())
                .andReturn());
        UUID commentId = UUID.fromString(createComment.get("id").asText());

        mockMvc.perform(post("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId)
                        .header("Authorization", bearer(curator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Resposta original"))))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId)
                        .header("Authorization", bearer(thirdParty))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Tentativa de terceiro"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReplyOnCommentWithNoReplyShouldReturnNotFound() throws Exception {
        User student = createActiveStudent();
        User curator = createActiveCurator();
        Article work = createArticleInDatabase(curator);

        JsonNode createComment = jsonFrom(mockMvc.perform(post("/work/{workId}/comments", work.getId())
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Comentario sem resposta"))))
                .andExpect(status().isCreated())
                .andReturn());
        UUID commentId = UUID.fromString(createComment.get("id").asText());

        mockMvc.perform(get("/work/{workId}/comments/{commentId}/reply", work.getId(), commentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
