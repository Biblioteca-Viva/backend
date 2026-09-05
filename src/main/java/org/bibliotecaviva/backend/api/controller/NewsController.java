package org.bibliotecaviva.backend.api.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.NewsRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.NewsResponseDTO;
import org.bibliotecaviva.backend.application.services.NewsService;
import org.bibliotecaviva.backend.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Tag(name = "News", description = "Gerenciamento de notícias da biblioteca. Criação e edição restritas a ADMIN e CURADOR. Leitura pública.")
public class NewsController {

    private final NewsService newsService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CURADOR')")
    @ApiResponse(responseCode = "201", description = "Notícia criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<NewsResponseDTO> create(
            @RequestPart("data") @Valid NewsRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newsService.create(dto, image, user));
    }

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Lista de notícias")
    public ResponseEntity<Page<NewsResponseDTO>> getAll(
            @Parameter(hidden = true)
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(newsService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "Notícia encontrada")
    @ApiResponse(responseCode = "404", description = "Notícia não encontrada")
    public ResponseEntity<NewsResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(newsService.getById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CURADOR')")
    @ApiResponse(responseCode = "200", description = "Notícia atualizada com sucesso")
    @ApiResponse(responseCode = "403", description = "Sem permissão para editar esta notícia")
    @ApiResponse(responseCode = "404", description = "Notícia não encontrada")
    public ResponseEntity<NewsResponseDTO> update(
            @PathVariable UUID id,
            @RequestPart("data") @Valid NewsRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(newsService.update(id, dto, image, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CURADOR')")
    @ApiResponse(responseCode = "204", description = "Notícia removida com sucesso")
    @ApiResponse(responseCode = "403", description = "Sem permissão para remover esta notícia")
    @ApiResponse(responseCode = "404", description = "Notícia não encontrada")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        newsService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
