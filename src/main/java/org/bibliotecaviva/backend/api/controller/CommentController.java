package org.bibliotecaviva.backend.api.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.CommentRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.CommentResponseDTO;
import org.bibliotecaviva.backend.application.services.CommentService;
import org.bibliotecaviva.backend.domain.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/work/{workId}/comments")
@RequiredArgsConstructor
@Tag(
        name = "Comments",
        description = "" +
                "User will be able to edit and deleted their own comments(but its not implemented), only admins can for now" +
                "Controller responsible for handling operations related to comments on works, including creating, " +
                "retrieving, updating, and deleting comments.")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Comment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "404", description = "Work not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized, user must be authenticated to create a comment")
    public ResponseEntity<CommentResponseDTO> create(
            @PathVariable UUID workId,
            @RequestBody @Valid CommentRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(workId, dto.content(), user));
    }

    @GetMapping
    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Work not found")
    public ResponseEntity<List<CommentResponseDTO>> getByWorkId(@PathVariable UUID workId) {
        return ResponseEntity.ok(commentService.getByWorkId(workId));
    }

    @PutMapping("/{commentId}")
    @ApiResponse(responseCode = "200", description = "Comment updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<CommentResponseDTO> update(
          //  @PathVariable UUID workId, comentei pq n tav usando
            @PathVariable UUID commentId,
            @RequestBody @Valid CommentRequestDTO dto) {
        return ResponseEntity.ok(commentService.update(commentId, dto.content()));
    }

    @DeleteMapping("/{commentId}")
    @ApiResponse(responseCode = "204", description = "Comment deleted")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<Void> delete(@PathVariable UUID commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}
