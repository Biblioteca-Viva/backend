package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.application.dtos.request.NewsRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.NewsResponseDTO;
import org.bibliotecaviva.backend.application.mappers.NewsMapper;
import org.bibliotecaviva.backend.domain.entities.News;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.ForbiddenException;
import org.bibliotecaviva.backend.domain.exceptions.NotFoundException;
import org.bibliotecaviva.backend.persistence.repository.NewsRepository;
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
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsMapper newsMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private NewsService newsService;

    // ──────────────────────────── create ────────────────────────────

    @Test
    void createShouldPersistNewsWithoutImage() {
        User author = buildUser(UUID.randomUUID(), Role.CURADOR);
        NewsRequestDTO dto = buildRequest("Título válido", "Conteúdo válido para teste");
        News mapped = buildNews(UUID.randomUUID(), author, null);
        NewsResponseDTO expected = buildResponse(mapped);

        when(newsMapper.toEntity(dto, author)).thenReturn(mapped);
        when(newsRepository.save(mapped)).thenReturn(mapped);
        when(newsMapper.toDto(mapped)).thenReturn(expected);

        NewsResponseDTO result = newsService.create(dto, null, author);

        assertSame(expected, result);
        verify(newsRepository).save(mapped);
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    void createShouldUploadImageAndSetUrlWhenImageIsProvided() {
        User author = buildUser(UUID.randomUUID(), Role.ADMIN);
        NewsRequestDTO dto = buildRequest("Título com foto", "Conteúdo longo o suficiente");
        News mapped = buildNews(UUID.randomUUID(), author, null);
        MockMultipartFile image = new MockMultipartFile(
                "image", "foto.jpg", "image/jpeg", "fake-image-bytes".getBytes());
        String cloudinaryUrl = "https://res.cloudinary.com/test/foto.jpg";
        NewsResponseDTO expected = buildResponse(mapped);

        when(newsMapper.toEntity(dto, author)).thenReturn(mapped);
        when(cloudinaryService.uploadImage(image)).thenReturn(cloudinaryUrl);
        when(newsRepository.save(mapped)).thenReturn(mapped);
        when(newsMapper.toDto(mapped)).thenReturn(expected);

        newsService.create(dto, image, author);

        assertEquals(cloudinaryUrl, mapped.getImageUrl());
        verify(cloudinaryService).uploadImage(image);
        verify(newsRepository).save(mapped);
    }

    @Test
    void createShouldSkipUploadWhenImageIsEmpty() {
        User author = buildUser(UUID.randomUUID(), Role.CURADOR);
        NewsRequestDTO dto = buildRequest("Título sem foto", "Conteúdo longo o suficiente");
        News mapped = buildNews(UUID.randomUUID(), author, null);
        MockMultipartFile emptyImage = new MockMultipartFile(
                "image", "foto.jpg", "image/jpeg", new byte[0]);
        NewsResponseDTO expected = buildResponse(mapped);

        when(newsMapper.toEntity(dto, author)).thenReturn(mapped);
        when(newsRepository.save(mapped)).thenReturn(mapped);
        when(newsMapper.toDto(mapped)).thenReturn(expected);

        newsService.create(dto, emptyImage, author);

        assertNull(mapped.getImageUrl());
        verifyNoInteractions(cloudinaryService);
    }

    // ──────────────────────────── getAll ────────────────────────────

    @Test
    void getAllShouldReturnPageOfNewsResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        User author = buildUser(UUID.randomUUID(), Role.ADMIN);
        News news = buildNews(UUID.randomUUID(), author, null);
        NewsResponseDTO expected = buildResponse(news);

        when(newsRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(news)));
        when(newsMapper.toDto(news)).thenReturn(expected);

        Page<NewsResponseDTO> result = newsService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(expected, result.getContent().getFirst());
    }

    @Test
    void getAllShouldReturnEmptyPageWhenNoNews() {
        Pageable pageable = PageRequest.of(0, 10);
        when(newsRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<NewsResponseDTO> result = newsService.getAll(pageable);

        assertTrue(result.isEmpty());
        verify(newsMapper, never()).toDto(any());
    }

    // ──────────────────────────── getById ────────────────────────────

    @Test
    void getByIdShouldReturnResponseWhenNewsExists() {
        UUID id = UUID.randomUUID();
        User author = buildUser(UUID.randomUUID(), Role.CURADOR);
        News news = buildNews(id, author, null);
        NewsResponseDTO expected = buildResponse(news);

        when(newsRepository.findById(id)).thenReturn(Optional.of(news));
        when(newsMapper.toDto(news)).thenReturn(expected);

        assertSame(expected, newsService.getById(id));
    }

    @Test
    void getByIdShouldThrowNotFoundWhenNewsDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(newsRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> newsService.getById(id));
    }

    // ──────────────────────────── update ────────────────────────────

    @Test
    void updateShouldApplyChangesForOwner() {
        UUID id = UUID.randomUUID();
        User author = buildUser(UUID.randomUUID(), Role.CURADOR);
        News news = buildNews(id, author, null);
        NewsRequestDTO dto = buildRequest("Novo título", "Novo conteúdo longo o suficiente");
        NewsResponseDTO expected = buildResponse(news);

        when(newsRepository.findById(id)).thenReturn(Optional.of(news));
        when(newsMapper.toDto(news)).thenReturn(expected);

        NewsResponseDTO result = newsService.update(id, dto, null, author);

        assertSame(expected, result);
        verify(newsMapper).partialUpdate(dto, news);
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    void updateShouldAllowAdminToEditAnyNews() {
        UUID id = UUID.randomUUID();
        User originalAuthor = buildUser(UUID.randomUUID(), Role.CURADOR);
        User admin = buildUser(UUID.randomUUID(), Role.ADMIN);
        News news = buildNews(id, originalAuthor, null);
        NewsRequestDTO dto = buildRequest("Título corrigido", "Conteúdo corrigido suficientemente");
        NewsResponseDTO expected = buildResponse(news);

        when(newsRepository.findById(id)).thenReturn(Optional.of(news));
        when(newsMapper.toDto(news)).thenReturn(expected);

        assertDoesNotThrow(() -> newsService.update(id, dto, null, admin));
        verify(newsMapper).partialUpdate(dto, news);
    }

    @Test
    void updateShouldThrowForbiddenWhenCuradorIsNotOwner() {
        UUID id = UUID.randomUUID();
        User originalAuthor = buildUser(UUID.randomUUID(), Role.CURADOR);
        User otherCurador = buildUser(UUID.randomUUID(), Role.CURADOR);
        News news = buildNews(id, originalAuthor, null);
        NewsRequestDTO dto = buildRequest("Título indevido", "Conteúdo indevido longo o suficiente");

        when(newsRepository.findById(id)).thenReturn(Optional.of(news));

        assertThrows(ForbiddenException.class, () -> newsService.update(id, dto, null, otherCurador));
        verify(newsMapper, never()).partialUpdate(any(), any());
    }

    @Test
    void updateShouldThrowNotFoundWhenNewsDoesNotExist() {
        UUID id = UUID.randomUUID();
        User author = buildUser(UUID.randomUUID(), Role.CURADOR);
        NewsRequestDTO dto = buildRequest("Qualquer título", "Qualquer conteúdo longo o suficiente");

        when(newsRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> newsService.update(id, dto, null, author));
    }

    @Test
    void updateShouldUploadNewImageWhenProvided() {
        UUID id = UUID.randomUUID();
        User author = buildUser(UUID.randomUUID(), Role.CURADOR);
        News news = buildNews(id, author, "https://res.cloudinary.com/old.jpg");
        NewsRequestDTO dto = buildRequest("Título atualizado", "Conteúdo atualizado longo o suficiente");
        MockMultipartFile image = new MockMultipartFile(
                "image", "nova.jpg", "image/jpeg", "new-image-bytes".getBytes());
        String newUrl = "https://res.cloudinary.com/nova.jpg";
        NewsResponseDTO expected = buildResponse(news);

        when(newsRepository.findById(id)).thenReturn(Optional.of(news));
        when(cloudinaryService.uploadImage(image)).thenReturn(newUrl);
        when(newsMapper.toDto(news)).thenReturn(expected);

        newsService.update(id, dto, image, author);

        assertEquals(newUrl, news.getImageUrl());
        verify(cloudinaryService).uploadImage(image);
    }

    // ──────────────────────────── delete ────────────────────────────

    @Test
    void deleteShouldAllowOwnerToDeleteOwnNews() {
        UUID id = UUID.randomUUID();
        User author = buildUser(UUID.randomUUID(), Role.CURADOR);
        News news = buildNews(id, author, null);

        when(newsRepository.findById(id)).thenReturn(Optional.of(news));

        assertDoesNotThrow(() -> newsService.delete(id, author));

        ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
        verify(newsRepository).delete(captor.capture());
        assertSame(news, captor.getValue());
    }

    @Test
    void deleteShouldAllowAdminToDeleteAnyNews() {
        UUID id = UUID.randomUUID();
        User originalAuthor = buildUser(UUID.randomUUID(), Role.CURADOR);
        User admin = buildUser(UUID.randomUUID(), Role.ADMIN);
        News news = buildNews(id, originalAuthor, null);

        when(newsRepository.findById(id)).thenReturn(Optional.of(news));

        assertDoesNotThrow(() -> newsService.delete(id, admin));
        verify(newsRepository).delete(news);
    }

    @Test
    void deleteShouldThrowForbiddenWhenCuradorIsNotOwner() {
        UUID id = UUID.randomUUID();
        User originalAuthor = buildUser(UUID.randomUUID(), Role.CURADOR);
        User otherCurador = buildUser(UUID.randomUUID(), Role.CURADOR);
        News news = buildNews(id, originalAuthor, null);

        when(newsRepository.findById(id)).thenReturn(Optional.of(news));

        assertThrows(ForbiddenException.class, () -> newsService.delete(id, otherCurador));
        verify(newsRepository, never()).delete(any());
    }

    @Test
    void deleteShouldThrowNotFoundWhenNewsDoesNotExist() {
        UUID id = UUID.randomUUID();
        User admin = buildUser(UUID.randomUUID(), Role.ADMIN);
        when(newsRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> newsService.delete(id, admin));
        verify(newsRepository, never()).delete(any());
    }

    // ──────────────────────────── helpers ────────────────────────────

    private static NewsRequestDTO buildRequest(String title, String content) {
        return new NewsRequestDTO(title, content);
    }

    private static News buildNews(UUID id, User author, String imageUrl) {
        return News.builder()
                .id(id)
                .title("Notícia de teste")
                .content("Conteúdo da notícia de teste para validação")
                .imageUrl(imageUrl)
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static NewsResponseDTO buildResponse(News news) {
        return new NewsResponseDTO(
                news.getId(),
                news.getTitle(),
                news.getContent(),
                news.getImageUrl(),
                news.getAuthor() != null ? news.getAuthor().getName() : null,
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }

    private static User buildUser(UUID id, Role role) {
        return User.builder()
                .id(id)
                .name(role == Role.ADMIN ? "Admin" : "Curador")
                .email(id + "@teste.com")
                .password("123456")
                .role(role)
                .accountStatus(Status.ACTIVE)
                .build();
    }
}
