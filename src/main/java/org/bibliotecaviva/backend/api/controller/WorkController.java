package org.bibliotecaviva.backend.api.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.LibraLiteratureRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.MultimediaRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.*;
import org.bibliotecaviva.backend.application.dtos.request.visual.ArtRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.visual.InfographicRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.WorkResponseDTO;
import org.bibliotecaviva.backend.application.services.WorkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/work")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService service;

    @GetMapping
    public ResponseEntity<List<WorkResponseDTO>> getAll(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(service.getAll(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/articles")
    public ResponseEntity<WorkResponseDTO> createArticle(@RequestBody @Valid ArticleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/articles/{id}")
    public ResponseEntity<WorkResponseDTO> updateArticle(@PathVariable UUID id, @RequestBody @Valid ArticleRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/cordels")
    public ResponseEntity<WorkResponseDTO> createCordel(@RequestBody @Valid CordelRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/cordels/{id}")
    public ResponseEntity<WorkResponseDTO> updateCordel(@PathVariable UUID id, @RequestBody @Valid CordelRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/essays")
    public ResponseEntity<WorkResponseDTO> createEssay(@RequestBody @Valid EssayRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/essays/{id}")
    public ResponseEntity<WorkResponseDTO> updateEssay(@PathVariable UUID id, @RequestBody @Valid EssayRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/short-stories")
    public ResponseEntity<WorkResponseDTO> createShortStory(@RequestBody @Valid ShortStoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/short-stories/{id}")
    public ResponseEntity<WorkResponseDTO> updateShortStory(@PathVariable UUID id, @RequestBody @Valid ShortStoryRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/tales")
    public ResponseEntity<WorkResponseDTO> createTale(@RequestBody @Valid TaleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/tales/{id}")
    public ResponseEntity<WorkResponseDTO> updateTale(@PathVariable UUID id, @RequestBody @Valid TaleRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/arts")
    public ResponseEntity<WorkResponseDTO> createArt(@RequestBody @Valid ArtRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/arts/{id}")
    public ResponseEntity<WorkResponseDTO> updateArt(@PathVariable UUID id, @RequestBody @Valid ArtRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/infographics")
    public ResponseEntity<WorkResponseDTO> createInfographic(@RequestBody @Valid InfographicRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/infographics/{id}")
    public ResponseEntity<WorkResponseDTO> updateInfographic(@PathVariable UUID id, @RequestBody @Valid InfographicRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/multimedias")
    public ResponseEntity<WorkResponseDTO> createMultimedia(@RequestBody @Valid MultimediaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/multimedias/{id}")
    public ResponseEntity<WorkResponseDTO> updateMultimedia(@PathVariable UUID id, @RequestBody @Valid MultimediaRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/libra-literatures")
    public ResponseEntity<WorkResponseDTO> createLibraLiterature(@RequestBody @Valid LibraLiteratureRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/libra-literatures/{id}")
    public ResponseEntity<WorkResponseDTO> updateLibraLiterature(@PathVariable UUID id, @RequestBody @Valid LibraLiteratureRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
}
