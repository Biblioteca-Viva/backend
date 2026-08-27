package org.bibliotecaviva.backend.integration;

import org.bibliotecaviva.backend.domain.entities.BookClub;
import org.bibliotecaviva.backend.domain.entities.BookClubReview;
import org.bibliotecaviva.backend.domain.entities.Comment;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.entities.textual.Article;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void adminShouldListCommentAndReviewSummaries() throws Exception {
        User admin = createActiveAdmin();
        User curator = createActiveCurator();
        User student = createActiveStudent();
        Article work = createArticleInDatabase(curator);
        Comment comment = createCommentInDatabase(student, work, "Summary comment");
        BookClub club = createBookClubInDatabase(curator, LocalDateTime.now().plusMonths(2));
        BookClubReview review = createBookClubReviewInDatabase(student, club, "Summary review", BigDecimal.valueOf(4));

        mockMvc.perform(get("/admin/comments").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]", comment.getId()).exists());
        mockMvc.perform(get("/admin/reviews").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]", review.getId()).exists());
    }

    @Test
    void adminShouldListUsersFilteredByStatus() throws Exception {
        User admin = createActiveAdmin();
        User pending = createPendingStudent();

        mockMvc.perform(get("/admin")
                        .header("Authorization", bearer(admin))
                        .queryParam("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(pending.getId().toString()))
                .andExpect(jsonPath("$.content[0].email").value(pending.getEmail()));
    }

    @Test
    void adminShouldApproveRejectAndBlockUsers() throws Exception {
        User admin = createActiveAdmin();
        String authorization = bearer(admin);
        User approveTarget = createPendingStudent();
        User rejectTarget = createPendingStudent();
        User blockTarget = createActiveStudent();

        mockMvc.perform(patch("/admin/approve/{id}", approveTarget.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());
        flushAndClear();
        assertEquals(Status.ACTIVE, userRepository.findById(approveTarget.getId()).orElseThrow().getAccountStatus());

        mockMvc.perform(patch("/admin/reject/{id}", rejectTarget.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());
        flushAndClear();
        assertEquals(Status.REJECTED, userRepository.findById(rejectTarget.getId()).orElseThrow().getAccountStatus());

        mockMvc.perform(patch("/admin/block/{id}", blockTarget.getId())
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());
        flushAndClear();
        assertEquals(Status.BLOCKED, userRepository.findById(blockTarget.getId()).orElseThrow().getAccountStatus());
    }

    @Test
    void adminShouldPermanentlyDeleteUserAndClearRelationships() throws Exception {
        User admin = createActiveAdmin();
        User target = createActiveCurator();
        User student = createActiveStudent();
        Article authoredWork = createArticleInDatabase(target, uniqueTitle("Obra autoral"));
        Article likedWork = createArticleInDatabase(admin, uniqueTitle("Obra curtida"));
        Comment targetComment = createCommentInDatabase(target, likedWork, "Comentario do usuario removido");
        Comment otherComment = createCommentInDatabase(student, likedWork, "Comentario de outro usuario");
        BookClub bookClub = createBookClubInDatabase(target, LocalDateTime.now().plusMonths(1));
        bookClub.getParticipants().add(target);
        bookClubRepository.saveAndFlush(bookClub);
        BookClubReview review = createBookClubReviewInDatabase(
                target,
                bookClub,
                "Resenha do usuario removido",
                BigDecimal.valueOf(4));
        userRepository.likeWork(target.getId(), likedWork.getId());
        commentRepository.likeComment(target.getId(), otherComment.getId());
        commentRepository.likeComment(student.getId(), targetComment.getId());
        flushAndClear();

        mockMvc.perform(delete("/admin/users/{id}", target.getId())
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
        flushAndClear();

        assertTrue(userRepository.findById(target.getId()).isEmpty());
        var persistedWork = workRepository.findById(authoredWork.getId()).orElseThrow();
        assertNull(persistedWork.getAuthor());
        assertEquals(target.getName(), persistedWork.getAuthorName());
        assertTrue(commentRepository.findById(targetComment.getId()).isEmpty());
        assertTrue(bookClubReviewRepository.findById(review.getId()).isEmpty());
        assertEquals(0L, workRepository.getLikeCount(likedWork.getId()));
        assertEquals(0L, commentRepository.getLikeCount(otherComment.getId()));
        var persistedClub = bookClubRepository.findById(bookClub.getId()).orElseThrow();
        assertNull(persistedClub.getOrganizer());
        assertEquals(0L, bookClubRepository.countParticipants(bookClub.getId()));
    }

    @Test
    void adminDashboardShouldReturnCurrentCounters() throws Exception {
        User admin = createActiveAdmin();
        createPendingStudent();
        User curator = createActiveCurator();
        User student = createActiveStudent();
        Article work = createArticleInDatabase(curator);
        createCommentInDatabase(student, work, "Comentario");

        mockMvc.perform(get("/admin/dashboard")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPosts").value(1))
                .andExpect(jsonPath("$.totalComments").value(1))
                .andExpect(jsonPath("$.totalUsers").value(4))
                .andExpect(jsonPath("$.pendingUsers").value(1));
    }

    @Test
    void nonAdminShouldNotAccessAdminEndpoints() throws Exception {
        User student = createActiveStudent();

        mockMvc.perform(get("/admin")
                        .header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());
    }
}
