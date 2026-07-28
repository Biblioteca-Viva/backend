package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.application.dtos.request.textual.ArticleRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.CordelRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.HomePageDashboardResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;
import org.bibliotecaviva.backend.application.dtos.response.WorkSummaryResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.textual.ArticleResponseDTO;
import org.bibliotecaviva.backend.application.mappers.WorkMapper;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.entities.projections.WorkSummary;
import org.bibliotecaviva.backend.domain.entities.textual.Article;
import org.bibliotecaviva.backend.domain.entities.textual.Cordel;
import org.bibliotecaviva.backend.domain.entities.visual.Art;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.enums.WorkTypes;
import org.bibliotecaviva.backend.domain.exceptions.UserNotFoundException;
import org.bibliotecaviva.backend.domain.exceptions.WorkNotFoundException;
import org.bibliotecaviva.backend.persistence.repository.CommentRepository;
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.bibliotecaviva.backend.persistence.repository.WorkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {

    @Mock
    private WorkRepository workRepository;

    @Mock
    private WorkMapper workMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private WorkService workService;

    @Test
    void getAllShouldFilterByTypeAndMapSummaries() {
        Pageable pageable = PageRequest.of(0, 10);
        WorkSummary projection = mock(WorkSummary.class);
        WorkSummaryResponseDTO summary = buildSummary("Article");
        when(workRepository.findAllSummary("Article", pageable)).thenReturn(new PageImpl<>(List.of(projection)));
        when(workMapper.toWorkSummary(projection)).thenReturn(summary);

        Page<WorkSummaryResponseDTO> response = workService.getAll(WorkTypes.ARTICLE, pageable);

        assertEquals(List.of(summary), response.getContent());
        verify(workRepository).findAllSummary("Article", pageable);
    }

    @Test
    void getAllShouldNotFilterWhenTypeIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        when(workRepository.findAllSummary(null, pageable)).thenReturn(Page.empty());

        Page<WorkSummaryResponseDTO> response = workService.getAll(null, pageable);

        assertEquals(0, response.getTotalElements());
        verify(workRepository).findAllSummary(null, pageable);
    }

    @Test
    void getByIdShouldIncrementViewCountAndMapWithCounts() {
        UUID id = UUID.randomUUID();
        User author = buildUser(UUID.randomUUID(), "autor@teste.com");
        Article work = buildArticle(id, author, "Obra");
        WorkResponse mapped = buildArticleResponse(id, "Obra", 4L, 2L);
        when(workRepository.findById(id)).thenReturn(Optional.of(work));
        when(workRepository.getLikeCount(id)).thenReturn(4L);
        when(commentRepository.countByWork_Id(id)).thenReturn(2L);
        when(workMapper.toDTO(work, 4L, 2L)).thenReturn(mapped);

        WorkResponse response = workService.getById(id);

        assertSame(mapped, response);
        verify(workRepository).incrementViewCount(id);
        verify(workMapper).toDTO(work, 4L, 2L);
    }

    @Test
    void getByIdShouldFailWhenWorkDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(workRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(WorkNotFoundException.class, () -> workService.getById(id));

        verify(workRepository, never()).incrementViewCount(id);
        verify(workMapper, never()).toDTO(any(), any(), any());
    }

    @Test
    void createShouldPersistMappedWorkWithAuthorAndInitialViewCount() {
        User author = buildUser(UUID.randomUUID(), "autor@teste.com");
        ArticleRequestDTO request = buildArticleRequest("Obra", author.getEmail());
        Article mapped = buildArticle(null, null, request.title());
        Article saved = buildArticle(UUID.randomUUID(), author, request.title());
        WorkResponse expected = buildArticleResponse(saved.getId(), request.title(), 0L, 0L);

        when(userRepository.findByEmail(request.authorEmail())).thenReturn(Optional.of(author));
        when(workMapper.toEntity(request)).thenReturn(mapped);
        when(workRepository.save(mapped)).thenReturn(saved);
        when(workMapper.toDTO(saved, 0L, 0L)).thenReturn(expected);

        WorkResponse response = workService.create(request);

        assertSame(expected, response);
        assertSame(author, mapped.getAuthor());
        assertEquals(0L, mapped.getViewCount());
        verify(workRepository).save(mapped);
    }

    @Test
    void createShouldFailWhenAuthorDoesNotExist() {
        ArticleRequestDTO request = buildArticleRequest("Obra", "naoexiste@teste.com");
        Article mapped = buildArticle(null, null, request.title());
        when(workMapper.toEntity(request)).thenReturn(mapped);
        when(userRepository.findByEmail(request.authorEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> workService.create(request));

        verify(workRepository, never()).save(any());
    }

    @Test
    void createCordelShouldLinkArtFoundByTitle() {
        CordelRequestDTO request = buildCordelRequest("Illustration");
        Cordel cordel = buildCordel(null, request.title());
        Art art = Art.builder().id(UUID.randomUUID()).title("Illustration").url("https://example.com/art.png").build();
        WorkResponse expected = mock(WorkResponse.class);
        when(workMapper.toEntity(request)).thenReturn(cordel);
        when(workRepository.findArtByTitle("Illustration")).thenReturn(Optional.of(art));
        when(workRepository.save(cordel)).thenReturn(cordel);
        when(workMapper.toDTO(cordel, 0L, 0L)).thenReturn(expected);

        WorkResponse response = workService.create(request);

        assertSame(expected, response);
        assertSame(art, cordel.getIllustration());
        assertEquals("External Author", cordel.getAuthorName());
        verify(workRepository).findArtByTitle("Illustration");
    }

    @Test
    void createCordelShouldFailWhenArtDoesNotExist() {
        CordelRequestDTO request = buildCordelRequest("Missing Art");
        Cordel cordel = buildCordel(null, request.title());
        when(workMapper.toEntity(request)).thenReturn(cordel);
        when(workRepository.findArtByTitle("Missing Art")).thenReturn(Optional.empty());

        assertThrows(WorkNotFoundException.class, () -> workService.create(request));

        verify(workRepository, never()).save(any());
    }

    @Test
    void createCordelShouldSkipArtLookupWhenArtNameIsBlank() {
        CordelRequestDTO request = buildCordelRequest(" ");
        Cordel cordel = buildCordel(null, request.title());
        WorkResponse expected = mock(WorkResponse.class);
        when(workMapper.toEntity(request)).thenReturn(cordel);
        when(workRepository.save(cordel)).thenReturn(cordel);
        when(workMapper.toDTO(cordel, 0L, 0L)).thenReturn(expected);

        assertSame(expected, workService.create(request));
        assertNull(cordel.getIllustration());
        verify(workRepository, never()).findArtByTitle(anyString());
    }

    @Test
    void updateShouldFailWhenAuthorEmailIsBlankAndNoUserMatches() {
        UUID id = UUID.randomUUID();
        User author = buildUser(UUID.randomUUID(), "autor@teste.com");
        Article work = buildArticle(id, author, "Obra antiga");
        ArticleRequestDTO request = buildArticleRequest("Obra atualizada", " ");

        when(workRepository.findById(id)).thenReturn(Optional.of(work));
        when(userRepository.findByEmail(request.authorEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> workService.update(id, request));
        verify(workMapper).partialUpdate(request, work);
        verify(workRepository, never()).save(any());
    }

    @Test
    void updateShouldChangeAuthorWhenAuthorEmailIsProvided() {
        UUID id = UUID.randomUUID();
        User oldAuthor = buildUser(UUID.randomUUID(), "antigo@teste.com");
        User newAuthor = buildUser(UUID.randomUUID(), "novo@teste.com");
        Article work = buildArticle(id, oldAuthor, "Obra");
        ArticleRequestDTO request = buildArticleRequest("Obra", newAuthor.getEmail());
        WorkResponse expected = buildArticleResponse(id, request.title(), 0L, 0L);

        when(workRepository.findById(id)).thenReturn(Optional.of(work));
        when(userRepository.findByEmail(newAuthor.getEmail())).thenReturn(Optional.of(newAuthor));
        when(workRepository.save(work)).thenReturn(work);
        when(workRepository.getLikeCount(id)).thenReturn(0L);
        when(commentRepository.countByWork_Id(id)).thenReturn(0L);
        when(workMapper.toDTO(work, 0L, 0L)).thenReturn(expected);

        WorkResponse response = workService.update(id, request);

        assertSame(expected, response);
        assertSame(newAuthor, work.getAuthor());
        verify(workMapper).partialUpdate(request, work);
    }

    @Test
    void updateShouldFailWhenWorkDoesNotExist() {
        UUID id = UUID.randomUUID();
        ArticleRequestDTO request = buildArticleRequest("Obra", "autor@teste.com");
        when(workRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(WorkNotFoundException.class, () -> workService.update(id, request));

        verify(workRepository, never()).save(any());
    }

    @Test
    void updateShouldFailWhenNewAuthorDoesNotExist() {
        UUID id = UUID.randomUUID();
        User author = buildUser(UUID.randomUUID(), "autor@teste.com");
        Article work = buildArticle(id, author, "Obra");
        ArticleRequestDTO request = buildArticleRequest("Obra", "naoexiste@teste.com");
        when(workRepository.findById(id)).thenReturn(Optional.of(work));
        when(userRepository.findByEmail(request.authorEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> workService.update(id, request));

        verify(workRepository, never()).save(any());
    }

    @Test
    void updateCordelShouldReplaceLinkedIllustration() {
        UUID id = UUID.randomUUID();
        CordelRequestDTO request = buildCordelRequest("New Art");
        Cordel cordel = buildCordel(id, "Old Cordel");
        Art art = Art.builder().id(UUID.randomUUID()).title("New Art").url("https://example.com/new.png").build();
        WorkResponse expected = mock(WorkResponse.class);
        when(workRepository.findById(id)).thenReturn(Optional.of(cordel));
        when(workRepository.findArtByTitle("New Art")).thenReturn(Optional.of(art));
        when(workRepository.save(cordel)).thenReturn(cordel);
        when(workRepository.getLikeCount(id)).thenReturn(1L);
        when(commentRepository.countByWork_Id(id)).thenReturn(2L);
        when(workMapper.toDTO(cordel, 1L, 2L)).thenReturn(expected);

        assertSame(expected, workService.update(id, request));
        assertSame(art, cordel.getIllustration());
        verify(workMapper).partialUpdate(request, cordel);
    }

    @Test
    void updateCordelShouldFailWhenRequestedArtDoesNotExist() {
        UUID id = UUID.randomUUID();
        CordelRequestDTO request = buildCordelRequest("Missing Art");
        Cordel cordel = buildCordel(id, "Cordel");
        when(workRepository.findById(id)).thenReturn(Optional.of(cordel));
        when(workRepository.findArtByTitle("Missing Art")).thenReturn(Optional.empty());

        assertThrows(WorkNotFoundException.class, () -> workService.update(id, request));
        verify(workRepository, never()).save(any());
    }

    @Test
    void deleteShouldDeleteExistingWork() {
        UUID id = UUID.randomUUID();
        Article work = buildArticle(id, buildUser(UUID.randomUUID(), "autor@teste.com"), "Obra");
        when(workRepository.findById(id)).thenReturn(Optional.of(work));

        workService.delete(id);

        verify(workRepository).deleteById(id);
    }

    @Test
    void deleteShouldFailWhenWorkDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(workRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(WorkNotFoundException.class, () -> workService.delete(id));

        verify(workRepository, never()).deleteById(id);
    }

    @Test
    void getFrontPageDataShouldAggregateCountsAndMappedHighlights() {
        WorkSummary recentArticle = mock(WorkSummary.class);
        WorkSummary mostLiked = mock(WorkSummary.class);
        WorkSummaryResponseDTO recentSummary = buildSummary("Article");
        WorkSummaryResponseDTO mostLikedSummary = buildSummary("Art");

        when(workRepository.countPerType()).thenReturn(List.of(
                new Object[]{"Article", 2L},
                new Object[]{"Art", 1L}
        ));
        when(workRepository.findTop5ByType(anyString())).thenReturn(List.of());
        when(workRepository.findTop5ByType(WorkTypes.ARTICLE.getValue())).thenReturn(List.of(recentArticle));
        when(workRepository.getMostLikedWorks()).thenReturn(List.of(mostLiked));
        when(workMapper.toWorkSummary(recentArticle)).thenReturn(recentSummary);
        when(workMapper.toWorkSummary(mostLiked)).thenReturn(mostLikedSummary);

        HomePageDashboardResponseDTO response = workService.getFrontPageData();

        assertEquals(2, response.articleCount());
        assertEquals(1, response.artCount());
        assertEquals(0, response.cordelCount());
        assertEquals(List.of(recentSummary), response.works());
        assertEquals(List.of(mostLikedSummary), response.mostLikedWorks());
        verify(workRepository, times(WorkTypes.values().length)).findTop5ByType(anyString());
    }

    @Test
    void countWorksShouldDelegateToRepository() {
        when(workRepository.count()).thenReturn(8L);

        assertEquals(8L, workService.countWorks());
    }

    private static ArticleRequestDTO buildArticleRequest(String title, String authorEmail) {
        return new ArticleRequestDTO(
                title,
                authorEmail,
                null,
                LocalDateTime.now().minusDays(1),
                "Descricao valida para teste",
                "Conteudo",
                "Turma A"
        );
    }

    private static CordelRequestDTO buildCordelRequest(String artName) {
        return new CordelRequestDTO(
                "Cordel Test", null, "External Author", LocalDateTime.now().minusDays(1),
                "Description valid for unit test", "Cordel content", "ABAB", artName, "Class A"
        );
    }

    private static User buildUser(UUID id, String email) {
        return User.builder()
                .id(id)
                .name("Autor")
                .email(email)
                .password("123456")
                .role(Role.CURADOR)
                .accountStatus(Status.ACTIVE)
                .build();
    }

    private static Article buildArticle(UUID id, User author, String title) {
        return Article.builder()
                .id(id)
                .title(title)
                .author(author)
                .publicationDate(LocalDateTime.now().minusDays(1))
                .description("Descricao")
                .content("Conteudo")
                .viewCount(0L)
                .build();
    }

    private static Cordel buildCordel(UUID id, String title) {
        return Cordel.builder().id(id).title(title).publicationDate(LocalDateTime.now().minusDays(1))
                .description("Description").content("Content").rhymeScheme("ABAB")
                .studentClass("Class A").viewCount(0L).build();
    }

    private static ArticleResponseDTO buildArticleResponse(UUID id, String title, Long likes, Long comments) {
        return new ArticleResponseDTO(
                id,
                title,
                "Autor",
                LocalDateTime.now().minusDays(1),
                "Descricao",
                "Article",
                "Conteudo",
                0L,
                likes,
                comments,
                "Turma A"
        );
    }

    private static WorkSummaryResponseDTO buildSummary(String type) {
        return new WorkSummaryResponseDTO(
                UUID.randomUUID(),
                "Resumo",
                "Autor",
                LocalDateTime.now().minusDays(1),
                "Descricao",
                type,
                0L,
                1L,
                1L,
                null,
                null,
                null
        );
    }
}
