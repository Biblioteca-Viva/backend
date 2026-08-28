package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.application.dtos.response.LikeResponseDTO;
import org.bibliotecaviva.backend.application.mappers.WorkMapper;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.entities.textual.Article;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.WorkNotFoundException;
import org.bibliotecaviva.backend.persistence.repository.CommentRepository;
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.bibliotecaviva.backend.persistence.repository.WorkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkServiceLikeTest {

    @Mock
    private WorkRepository workRepository;

    @Mock
    private WorkMapper workMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private WorkService workService;

    @Test
    void likeShouldReturnLikedTrueAndCurrentLikeCount() {
        UUID workId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        Article work = buildArticle(workId, user);

        when(workRepository.findById(workId)).thenReturn(Optional.of(work));
        when(workRepository.getLikeCount(workId)).thenReturn(1L);

        LikeResponseDTO response = workService.like(workId, user);

        assertTrue(response.liked());
        assertEquals(1L, response.likeCount());
        verify(userRepository).likeWork(userId, workId);
    }

    @Test
    void unlikeShouldReturnLikedFalseAndCurrentLikeCount() {
        UUID workId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        Article work = buildArticle(workId, user);

        when(workRepository.findById(workId)).thenReturn(Optional.of(work));
        when(workRepository.getLikeCount(workId)).thenReturn(0L);

        LikeResponseDTO response = workService.unLike(workId, user);

        assertFalse(response.liked());
        assertEquals(0L, response.likeCount());
        verify(userRepository).unlikeWork(userId, workId);
    }

    @Test
    void likeShouldFailWhenWorkDoesNotExist() {
        UUID workId = UUID.randomUUID();
        User user = buildUser(UUID.randomUUID());

        when(workRepository.findById(workId)).thenReturn(Optional.empty());

        assertThrows(WorkNotFoundException.class, () -> workService.like(workId, user));
        verify(userRepository, never()).likeWork(user.getId(), workId);
    }

    @Test
    void unlikeShouldFailWhenWorkDoesNotExist() {
        UUID workId = UUID.randomUUID();
        User user = buildUser(UUID.randomUUID());

        when(workRepository.findById(workId)).thenReturn(Optional.empty());

        assertThrows(WorkNotFoundException.class, () -> workService.unLike(workId, user));
        verify(userRepository, never()).unlikeWork(user.getId(), workId);
    }

    @Test
    void getLikedWorkIdsShouldReturnUserLikedWorkIds() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        List<UUID> likedWorkIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(userRepository.findLikedWorkIdsByUserId(userId)).thenReturn(likedWorkIds);

        assertEquals(likedWorkIds, workService.getLikedWorkIds(user));
        verify(userRepository).findLikedWorkIdsByUserId(userId);
    }

    private static User buildUser(UUID userId) {
        return User.builder()
                .id(userId)
                .name("admin")
                .email("admin@teste.com")
                .password("123456")
                .role(Role.ADMIN)
                .accountStatus(Status.ACTIVE)
                .build();
    }

    private static Article buildArticle(UUID workId, User user) {
        return Article.builder()
                .id(workId)
                .title("Obra de teste")
                .author(user)
                .publicationDate(LocalDateTime.now())
                .description("Descricao")
                .content("Conteudo")
                .viewCount(0L)
                .build();
    }
}
