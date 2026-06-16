package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.application.dtos.response.CommentReplyResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.CommentResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.CommentSummaryResponseDTO;
import org.bibliotecaviva.backend.domain.entities.Comment;
import org.bibliotecaviva.backend.domain.entities.CommentReply;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.entities.projections.CommentSummary;
import org.bibliotecaviva.backend.domain.entities.textual.Article;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.CommentNotFoundException;
import org.bibliotecaviva.backend.domain.exceptions.ReplyAlreadyExistsException;
import org.bibliotecaviva.backend.domain.exceptions.WorkNotFoundException;
import org.bibliotecaviva.backend.persistence.repository.CommentReplyRepository;
import org.bibliotecaviva.backend.persistence.repository.CommentRepository;
import org.bibliotecaviva.backend.persistence.repository.WorkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private WorkRepository workRepository;

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentReplyRepository replyRepository;

    @Test
    void createShouldPersistCommentForExistingWork() {
        UUID workId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        User user = buildUser(UUID.randomUUID(), Role.ALUNO);
        Article work = buildArticle(workId, user);
        LocalDateTime createdAt = LocalDateTime.now();
        when(workRepository.findById(workId)).thenReturn(Optional.of(work));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(commentId);
            comment.setCreatedAt(createdAt);
            return comment;
        });

        CommentResponseDTO response = commentService.create(workId, "Comentario", user);

        assertEquals(commentId, response.id());
        assertEquals("Comentario", response.content());
        assertEquals(user.getName(), response.authorName());
        assertEquals(createdAt, response.createdAt());

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        assertEquals("Comentario", commentCaptor.getValue().getContent());
        assertSame(user, commentCaptor.getValue().getUser());
        assertSame(work, commentCaptor.getValue().getWork());
    }

    @Test
    void createShouldFailWhenWorkDoesNotExist() {
        UUID workId = UUID.randomUUID();
        User user = buildUser(UUID.randomUUID(), Role.ALUNO);
        when(workRepository.findById(workId)).thenReturn(Optional.empty());

        assertThrows(WorkNotFoundException.class, () -> commentService.create(workId, "Comentario", user));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void getByWorkIdShouldReturnCommentsOrderedByRepository() {
        UUID workId = UUID.randomUUID();
        User user = buildUser(UUID.randomUUID(), Role.ALUNO);
        Article work = buildArticle(workId, user);
        Comment comment = buildComment(UUID.randomUUID(), "Comentario", user, work);
        Pageable pageable = PageRequest.of(0, 10);
        when(workRepository.existsById(workId)).thenReturn(true);
        when(commentRepository.findByWorkIdOrderByCreatedAtDesc(workId, pageable)).thenReturn(new PageImpl<>(List.of(comment)));

        Page<CommentResponseDTO> response = commentService.getByWorkId(workId, pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(comment.getContent(), response.getContent().getFirst().content());
        verify(commentRepository).findByWorkIdOrderByCreatedAtDesc(workId, pageable);
    }

    @Test
    void getByWorkIdShouldFailWhenWorkDoesNotExist() {
        UUID workId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        when(workRepository.existsById(workId)).thenReturn(false);

        assertThrows(WorkNotFoundException.class, () -> commentService.getByWorkId(workId, pageable));

        verify(commentRepository, never()).findByWorkIdOrderByCreatedAtDesc(workId, pageable);
    }

    @Test
    void getAllShouldMapCommentSummaries() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID commentId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        CommentSummary summary = mock(CommentSummary.class);
        when(summary.getId()).thenReturn(commentId);
        when(summary.getContent()).thenReturn("Comentario");
        when(summary.getUserName()).thenReturn("Aluno");
        when(summary.getWorkTitle()).thenReturn("Obra");
        when(summary.getCreatedAt()).thenReturn(createdAt);
        when(commentRepository.findAllWithDetails(pageable)).thenReturn(new PageImpl<>(List.of(summary)));
        Page<CommentSummaryResponseDTO> response = commentService.getAll(pageable);

        assertEquals(1, response.getTotalElements());
        CommentSummaryResponseDTO dto = response.getContent().getFirst();
        assertEquals(commentId, dto.id());
        assertEquals("Comentario", dto.content());
        assertEquals("Aluno", dto.userName());
        assertEquals("Obra", dto.workTitle());
        assertEquals(createdAt, dto.createdAt());
    }

    @Test
    void updateShouldAllowCommentOwner() {
        UUID commentId = UUID.randomUUID();
        User user = buildUser(UUID.randomUUID(), Role.ALUNO);
        Article work = buildArticle(UUID.randomUUID(), user);
        Comment comment = buildComment(commentId, "Antigo", user, work);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        CommentResponseDTO response = commentService.update(commentId, user, "Atualizado");

        assertEquals("Atualizado", comment.getContent());
        assertEquals("Atualizado", response.content());
        verify(commentRepository).save(comment);
    }

    @Test
    void updateShouldFailWhenUserIsNotOwner() {
        UUID commentId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);
        User other = buildUser(UUID.randomUUID(), Role.ALUNO);
        Article work = buildArticle(UUID.randomUUID(), owner);
        Comment comment = buildComment(commentId, "Comentario", owner, work);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.update(commentId, other, "Atualizado"));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateShouldFailWhenCommentDoesNotExist() {
        UUID commentId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.update(commentId, buildUser(UUID.randomUUID(), Role.ALUNO), "Atualizado"));
    }

    @Test
    void deleteShouldAllowCommentOwner() {
        UUID commentId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);
        Article work = buildArticle(UUID.randomUUID(), owner);
        Comment comment = buildComment(commentId, "Comentario", owner, work);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.delete(commentId, owner);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteShouldAllowAdmin() {
        UUID commentId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);
        User admin = buildUser(UUID.randomUUID(), Role.ADMIN);
        Article work = buildArticle(UUID.randomUUID(), owner);
        Comment comment = buildComment(commentId, "Comentario", owner, work);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.delete(commentId, admin);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteShouldFailWhenUserIsNotOwnerOrAdmin() {
        UUID commentId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);
        User other = buildUser(UUID.randomUUID(), Role.ALUNO);
        Article work = buildArticle(UUID.randomUUID(), owner);
        Comment comment = buildComment(commentId, "Comentario", owner, work);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.delete(commentId, other));

        verify(commentRepository, never()).deleteById(commentId);
    }

    @Test
    void deleteShouldFailWhenCommentDoesNotExist() {
        UUID commentId = UUID.randomUUID();
        User user = buildUser(UUID.randomUUID(), Role.ADMIN);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.delete(commentId, user));

        verify(commentRepository, never()).deleteById(commentId);
    }

    @Test
    void countCommentsShouldDelegateToRepository() {
        when(commentRepository.count()).thenReturn(6L);

        assertEquals(6L, commentService.countComments());
    }

    private static User buildUser(UUID id, Role role) {
        return User.builder()
                .id(id)
                .name(role == Role.ADMIN ? "Admin" : "Aluno")
                .email(id + "@teste.com")
                .password("123456")
                .role(role)
                .accountStatus(Status.ACTIVE)
                .build();
    }

    private static Article buildArticle(UUID id, User author) {
        return Article.builder()
                .id(id)
                .title("Obra")
                .author(author)
                .publicationDate(LocalDateTime.now().minusDays(1))
                .description("Descricao")
                .content("Conteudo")
                .viewCount(0L)
                .build();
    }

    private static Comment buildComment(UUID id, String content, User user, Article work) {
        return Comment.builder()
                .id(id)
                .content(content)
                .user(user)
                .work(work)
                .createdAt(LocalDateTime.now())
                .build();
    }

    //replies
    @Test
    void replyShouldAllowAdmin() {
        User workAuthor = buildUser(UUID.randomUUID(), Role.ALUNO);
        User admin = buildUser(UUID.randomUUID(), Role.ADMIN);
        Article work = buildArticle(UUID.randomUUID(), workAuthor);
        Comment comment = buildComment(UUID.randomUUID(), "Comentario", workAuthor, work);
        UUID replyId = UUID.randomUUID();
        LocalDateTime replyCreatedAt = LocalDateTime.now();

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(replyRepository.save(any(CommentReply.class))).thenAnswer(inv -> {
            CommentReply reply = inv.getArgument(0);
            reply.setId(replyId);
            reply.setCreatedAt(replyCreatedAt);
            return reply;
        });

        CommentReplyResponseDTO response = commentService.reply(comment.getId(), "Resposta do admin", admin);

        assertEquals(replyId, response.id());
        assertEquals("Resposta do admin", response.content());
        assertEquals(admin.getName(), response.authorName());
        assertEquals(replyCreatedAt, response.createdAt());
        verify(replyRepository).save(any(CommentReply.class));
    }

    @Test
    void replyShouldAllowWorkAuthor() {
        User workAuthor = buildUser(UUID.randomUUID(), Role.ALUNO);
        User commenter = buildUser(UUID.randomUUID(), Role.ALUNO);
        Article work = buildArticle(UUID.randomUUID(), workAuthor);
        Comment comment = buildComment(UUID.randomUUID(), "Comentario", commenter, work);
        UUID replyId = UUID.randomUUID();
        LocalDateTime replyCreatedAt = LocalDateTime.now();

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(replyRepository.save(any(CommentReply.class))).thenAnswer(inv -> {
            CommentReply reply = inv.getArgument(0);
            reply.setId(replyId);
            reply.setCreatedAt(replyCreatedAt);
            return reply;
        });

        CommentReplyResponseDTO response = commentService.reply(comment.getId(), "Resposta do autor", workAuthor);

        assertEquals("Resposta do autor", response.content());
        assertEquals(workAuthor.getName(), response.authorName());
        verify(replyRepository).save(any(CommentReply.class));
    }

    @Test
    void replyShouldFailWhenUserIsNeitherAdminNorWorkAuthor() {
        User workAuthor = buildUser(UUID.randomUUID(), Role.ALUNO);
        User thirdParty = buildUser(UUID.randomUUID(), Role.ALUNO);
        Article work = buildArticle(UUID.randomUUID(), workAuthor);
        Comment comment = buildComment(UUID.randomUUID(), "Comentario", thirdParty, work);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class,
                () -> commentService.reply(comment.getId(), "Tentativa", thirdParty));

        verify(replyRepository, never()).save(any());
    }

    @Test
    void replyShouldFailWhenCommentAlreadyHasReply() {
        User workAuthor = buildUser(UUID.randomUUID(), Role.ALUNO);
        Article work = buildArticle(UUID.randomUUID(), workAuthor);
        Comment comment = buildComment(UUID.randomUUID(), "Comentario", workAuthor, work);
        CommentReply existingReply = CommentReply.builder()
                .id(UUID.randomUUID())
                .content("Resposta existente")
                .user(workAuthor)
                .comment(comment)
                .build();
        comment.setReply(existingReply);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        assertThrows(ReplyAlreadyExistsException.class,
                () -> commentService.reply(comment.getId(), "Segunda resposta", workAuthor));

        verify(replyRepository, never()).save(any());
    }

    @Test
    void replyShouldFailWhenCommentDoesNotExist() {
        UUID commentId = UUID.randomUUID();
        User admin = buildUser(UUID.randomUUID(), Role.ADMIN);

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class,
                () -> commentService.reply(commentId, "Resposta", admin));

        verify(replyRepository, never()).save(any());
    }

    // =========================================================
    // getByReplyByCommentId()
    // =========================================================

    @Test
    void getByReplyByCommentIdShouldReturnReplyWhenExists() {
        UUID commentId = UUID.randomUUID();
        User author = buildUser(UUID.randomUUID(), Role.ALUNO);
        CommentReply reply = CommentReply.builder()
                .id(UUID.randomUUID())
                .content("Resposta")
                .user(author)
                .createdAt(LocalDateTime.now())
                .build();

        when(commentRepository.existsById(commentId)).thenReturn(true);
        when(replyRepository.findByCommentId(commentId)).thenReturn(Optional.of(reply));

        CommentReplyResponseDTO response = commentService.getByReplyByCommentId(commentId);

        assertEquals(reply.getId(), response.id());
        assertEquals("Resposta", response.content());
        assertEquals(author.getName(), response.authorName());
    }

    @Test
    void getByReplyByCommentIdShouldFailWhenCommentDoesNotExist() {
        UUID commentId = UUID.randomUUID();

        when(commentRepository.existsById(commentId)).thenReturn(false);

        assertThrows(CommentNotFoundException.class,
                () -> commentService.getByReplyByCommentId(commentId));

        verify(replyRepository, never()).findByCommentId(any());
    }

    @Test
    void getByReplyByCommentIdShouldFailWhenReplyDoesNotExist() {
        UUID commentId = UUID.randomUUID();

        when(commentRepository.existsById(commentId)).thenReturn(true);
        when(replyRepository.findByCommentId(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class,
                () -> commentService.getByReplyByCommentId(commentId));
    }

    // =========================================================
    // deleteReply()
    // =========================================================

    @Test
    void deleteReplyShouldAllowOwner() {
        UUID replyId = UUID.randomUUID();

        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);

        Comment comment = buildComment(
                UUID.randomUUID(),
                "Muito bom",
                buildUser(UUID.randomUUID(), Role.ALUNO),
                buildArticle(UUID.randomUUID(), buildUser(UUID.randomUUID(), Role.ALUNO))
        );

        CommentReply reply = CommentReply.builder()
                .id(replyId)
                .content("Resposta")
                .user(owner)
                .comment(comment)
                .build();

        comment.setReply(reply);

        when(replyRepository.findById(replyId))
                .thenReturn(Optional.of(reply));

        commentService.deleteReply(replyId, owner);

        assertNull(comment.getReply());
    }

    @Test
    void deleteReplyShouldAllowAdmin() {
        UUID replyId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);
        User admin = buildUser(UUID.randomUUID(), Role.ADMIN);

        Comment comment = buildComment(
                UUID.randomUUID(),
                "Muito bom",
                buildUser(UUID.randomUUID(), Role.ALUNO),
                buildArticle(UUID.randomUUID(), buildUser(UUID.randomUUID(), Role.ALUNO))
        );
        CommentReply reply = CommentReply.builder()
                .id(replyId)
                .content("Resposta")
                .user(owner)
                .comment(comment)
                .build();

        comment.setReply(reply);

        when(replyRepository.findById(replyId))
                .thenReturn(Optional.of(reply));

        commentService.deleteReply(replyId, admin);

        assertNull(comment.getReply());
    }

    @Test
    void deleteReplyShouldFailWhenUserIsNeitherOwnerNorAdmin() {
        UUID replyId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);
        User thirdParty = buildUser(UUID.randomUUID(), Role.ALUNO);
        CommentReply reply = CommentReply.builder()
                .id(replyId)
                .content("Resposta")
                .user(owner)
                .build();

        when(replyRepository.findById(replyId)).thenReturn(Optional.of(reply));

        assertThrows(AccessDeniedException.class,
                () -> commentService.deleteReply(replyId, thirdParty));

        verify(replyRepository, never()).delete(any());
    }

    @Test
    void deleteReplyShouldFailWhenReplyDoesNotExist() {
        UUID replyId = UUID.randomUUID();
        User admin = buildUser(UUID.randomUUID(), Role.ADMIN);

        when(replyRepository.findById(replyId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class,
                () -> commentService.deleteReply(replyId, admin));

        verify(replyRepository, never()).delete(any());
    }

    // =========================================================
    // updateReply()
    // =========================================================

    @Test
    void updateReplyShouldAllowOwner() {
        UUID commentId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);
        CommentReply reply = CommentReply.builder()
                .id(UUID.randomUUID())
                .content("Antigo")
                .user(owner)
                .createdAt(LocalDateTime.now())
                .build();

        when(replyRepository.findByCommentId(commentId)).thenReturn(Optional.of(reply));
        when(replyRepository.save(reply)).thenReturn(reply);

        CommentReplyResponseDTO response = commentService.updateReply(commentId, "Atualizado", owner);

        assertEquals("Atualizado", reply.getContent());
        assertEquals("Atualizado", response.content());
        verify(replyRepository).save(reply);
    }

    @Test
    void updateReplyShouldAllowAdmin() {
        UUID commentId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);
        User admin = buildUser(UUID.randomUUID(), Role.ADMIN);
        CommentReply reply = CommentReply.builder()
                .id(UUID.randomUUID())
                .content("Antigo")
                .user(owner)
                .createdAt(LocalDateTime.now())
                .build();

        when(replyRepository.findByCommentId(commentId)).thenReturn(Optional.of(reply));
        when(replyRepository.save(reply)).thenReturn(reply);

        CommentReplyResponseDTO response = commentService.updateReply(commentId, "Atualizado pelo admin", admin);

        assertEquals("Atualizado pelo admin", reply.getContent());
        assertEquals("Atualizado pelo admin", response.content());
        verify(replyRepository).save(reply);
    }

    @Test
    void updateReplyShouldFailWhenUserIsNeitherOwnerNorAdmin() {
        UUID commentId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);
        User thirdParty = buildUser(UUID.randomUUID(), Role.ALUNO);
        CommentReply reply = CommentReply.builder()
                .id(UUID.randomUUID())
                .content("Antigo")
                .user(owner)
                .build();

        when(replyRepository.findByCommentId(commentId)).thenReturn(Optional.of(reply));

        assertThrows(AccessDeniedException.class,
                () -> commentService.updateReply(commentId, "Tentativa", thirdParty));

        verify(replyRepository, never()).save(any());
    }

    @Test
    void updateReplyShouldFailWhenReplyDoesNotExist() {
        UUID commentId = UUID.randomUUID();
        User owner = buildUser(UUID.randomUUID(), Role.ALUNO);

        when(replyRepository.findByCommentId(commentId)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class,
                () -> commentService.updateReply(commentId, "Atualizado", owner));

        verify(replyRepository, never()).save(any());
    }

}
