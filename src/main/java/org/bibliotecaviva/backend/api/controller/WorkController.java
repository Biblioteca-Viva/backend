package org.bibliotecaviva.backend.api.controller;


import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.LibraLiteratureRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.MultimediaRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.*;
import org.bibliotecaviva.backend.application.dtos.request.visual.ArtRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.visual.InfographicRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;
import org.bibliotecaviva.backend.application.dtos.response.WorkResponseDTO;
import org.bibliotecaviva.backend.application.services.WorkService;
import org.bibliotecaviva.backend.domain.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/work")
@RequiredArgsConstructor
public class WorkController {
    //TODO: PATCH FOR PARTIAL UPDATES IF NEEDED, soft delete com status nas obras
    private final WorkService service;

    @GetMapping
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = WorkResponseDTO.class)))
    public ResponseEntity<List<WorkResponse>> getAll(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(service.getAll(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<String> likeWork(@PathVariable UUID id,
                           @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(service.like(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/articles")
    public ResponseEntity<WorkResponse> createArticle(@RequestBody @Valid ArticleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/articles/{id}")
    public ResponseEntity<WorkResponse> updateArticle(@PathVariable UUID id, @RequestBody @Valid ArticleRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/cordels")
    public ResponseEntity<WorkResponse> createCordel(@RequestBody @Valid CordelRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/cordels/{id}")
    public ResponseEntity<WorkResponse> updateCordel(@PathVariable UUID id, @RequestBody @Valid CordelRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/essays")
    public ResponseEntity<WorkResponse> createEssay(@RequestBody @Valid EssayRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/essays/{id}")
    public ResponseEntity<WorkResponse> updateEssay(@PathVariable UUID id, @RequestBody @Valid EssayRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/short-stories")
    public ResponseEntity<WorkResponse> createShortStory(@RequestBody @Valid ShortStoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/short-stories/{id}")
    public ResponseEntity<WorkResponse> updateShortStory(@PathVariable UUID id, @RequestBody @Valid ShortStoryRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/tales")
    public ResponseEntity<WorkResponse> createTale(@RequestBody @Valid TaleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/tales/{id}")
    public ResponseEntity<WorkResponse> updateTale(@PathVariable UUID id, @RequestBody @Valid TaleRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/arts")
    public ResponseEntity<WorkResponse> createArt(@RequestBody @Valid ArtRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/arts/{id}")
    public ResponseEntity<WorkResponse> updateArt(@PathVariable UUID id, @RequestBody @Valid ArtRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/infographics")
    public ResponseEntity<WorkResponse> createInfographic(@RequestBody @Valid InfographicRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/infographics/{id}")
    public ResponseEntity<WorkResponse> updateInfographic(@PathVariable UUID id, @RequestBody @Valid InfographicRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/multimedias")
    public ResponseEntity<WorkResponse> createMultimedia(@RequestBody @Valid MultimediaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/multimedias/{id}")
    public ResponseEntity<WorkResponse> updateMultimedia(@PathVariable UUID id, @RequestBody @Valid MultimediaRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/libra-literatures")
    public ResponseEntity<WorkResponse> createLibraLiterature(@RequestBody @Valid LibraLiteratureRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/libra-literatures/{id}")
    public ResponseEntity<WorkResponse> updateLibraLiterature(@PathVariable UUID id, @RequestBody @Valid LibraLiteratureRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
}
