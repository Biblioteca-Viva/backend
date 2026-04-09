package org.bibliotecaviva.backend.api.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.LibraLiteratureRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.audiovisual.MultimediaRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.textual.*;
import org.bibliotecaviva.backend.application.dtos.request.visual.ArtRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.visual.InfographicRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.LikeResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.WorkResponse;
import org.bibliotecaviva.backend.application.dtos.response.WorkSummaryResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.audiovisual.LibraLiteratureResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.audiovisual.MultimediaResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.textual.*;
import org.bibliotecaviva.backend.application.dtos.response.visual.ArtResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.visual.InfographicReponseDTO;
import org.bibliotecaviva.backend.application.services.WorkService;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.WorkTypes;
import org.bibliotecaviva.backend.domain.exceptions.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/work")
@RequiredArgsConstructor
@Tag(name = "Works",
        description = """
                Author in Post method are emails for now.
                To create works,the type of work is determined by the DTO sent in the request body. The system will automatically
                determine the type based on the fields present in the DTO. For example, if you send an ArticleRequestDTO, 
                it will create an article. If you send a CordelRequestDTO, it will create a cordel, and so on.
                """
)
@ApiResponse(responseCode = "ErrorMessage", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)),
        description = "Error message that will show when an exception is thrown")

public class WorkController {
    //TODO: PATCH FOR PARTIAL UPDATES IF NEEDED
    private final WorkService service;

    @GetMapping
    @Operation(description = "Return a summary of works with generic infos that are shared")
    @Parameter(name = "type", description = "Filter by type of work (case insensitive).")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = WorkSummaryResponseDTO.class)))
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid Parameter")
    public ResponseEntity<List<WorkSummaryResponseDTO>> getAll(@RequestParam(required = false) WorkTypes type) {
        return ResponseEntity.ok(service.getAll(type));
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = WorkResponse.class)))
    @ApiResponse(responseCode = "404", content = @Content, description = "Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }


    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", content = @Content, description = "No Content, successfully deleted")
    @ApiResponse(responseCode = "404", content = @Content, description = "Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    @ApiResponse(responseCode = "200", content = @Content, description = "Liked or disliked")
    @ApiResponse(responseCode = "404", content = @Content, description = "Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<LikeResponseDTO> likeWork(@PathVariable UUID id,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.like(id, user));
    }

    @PostMapping("/articles")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ArticleResponseDTO.class))
            , description = "Created")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> createArticle(@RequestBody @Valid ArticleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/articles/{id}")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ArticleResponseDTO.class))
            , description = "Updated")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Work or Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> updateArticle(@PathVariable UUID id, @RequestBody @Valid ArticleRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/cordels")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = CordelResponseDTO.class)),
            description = "Created")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> createCordel(@RequestBody @Valid CordelRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/cordels/{id}")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CordelResponseDTO.class)),
            description = "Updated")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Work or Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> updateCordel(@PathVariable UUID id, @RequestBody @Valid CordelRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/essays")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = EssayResponseDTO.class)),
            description = "Created")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> createEssay(@RequestBody @Valid EssayRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/essays/{id}")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = EssayResponseDTO.class)),
            description = "Updated")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Wokr or Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> updateEssay(@PathVariable UUID id, @RequestBody @Valid EssayRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/short-stories")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ShortStoryResponseDTO.class)),
            description = "Created")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> createShortStory(@RequestBody @Valid ShortStoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/short-stories/{id}")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ShortStoryResponseDTO.class)),
            description = "Updated")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Work or Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> updateShortStory(@PathVariable UUID id, @RequestBody @Valid ShortStoryRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/tales")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = TaleResponseDTO.class)),
            description = "Created")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> createTale(@RequestBody @Valid TaleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/tales/{id}")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TaleResponseDTO.class)),
            description = "Updated")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Work or Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> updateTale(@PathVariable UUID id, @RequestBody @Valid TaleRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/arts")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ArtResponseDTO.class)),
            description = "Created")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> createArt(@RequestBody @Valid ArtRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/arts/{id}")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ArtResponseDTO.class)),
            description = "Updated")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Work or  Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> updateArt(@PathVariable UUID id, @RequestBody @Valid ArtRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/infographics")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = InfographicReponseDTO.class)), description = "Created")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> createInfographic(@RequestBody @Valid InfographicRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/infographics/{id}")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = InfographicReponseDTO.class)),
            description = "Updated")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Work or Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> updateInfographic(@PathVariable UUID id, @RequestBody @Valid InfographicRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/multimedias")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = MultimediaResponseDTO.class)), description = "Created")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> createMultimedia(@RequestBody @Valid MultimediaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/multimedias/{id}")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MultimediaResponseDTO.class)),
            description = "Updated")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Work or Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> updateMultimedia(@PathVariable UUID id, @RequestBody @Valid MultimediaRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/libra-literatures")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = LibraLiteratureResponseDTO.class)), description = "Created")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> createLibraLiterature(@RequestBody @Valid LibraLiteratureRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/libra-literatures/{id}")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LibraLiteratureResponseDTO.class)),
            description = "Updated")
    @ApiResponse(responseCode = "409", content = @Content, description = "Work Already Exists")
    @ApiResponse(responseCode = "404", content = @Content, description = "Work or Author Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<WorkResponse> updateLibraLiterature(@PathVariable UUID id, @RequestBody @Valid LibraLiteratureRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }
}
