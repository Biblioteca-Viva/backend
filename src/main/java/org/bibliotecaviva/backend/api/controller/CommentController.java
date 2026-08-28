package org.bibliotecaviva.backend.api.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.CommentReplyRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.CommentRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.CommentReplyResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.CommentResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.LikeResponseDTO;
import org.bibliotecaviva.backend.application.services.CommentService;
import org.bibliotecaviva.backend.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/work/{workId}/comments")
@RequiredArgsConstructor
@Tag(
        name = "Comments",
        description = "Operations for comments and their replies on published works.")
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
    public ResponseEntity<Page<CommentResponseDTO>> getByWorkId(
            @PathVariable UUID workId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(commentService.getByWorkId(workId, pageable));
    }

    @PutMapping("/{commentId}")
    @ApiResponse(responseCode = "200", description = "Comment updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<CommentResponseDTO> update(
            @PathVariable UUID commentId,
            @RequestBody @Valid CommentRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(commentService.update(commentId, user, dto.content()));
    }

    @DeleteMapping("/{commentId}")
    @ApiResponse(responseCode = "204", description = "Comment deleted")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<Void> delete(@PathVariable UUID commentId,
                                       @AuthenticationPrincipal User user) {
        commentService.delete(commentId, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{commentId}/like")
    @ApiResponse(responseCode = "200", content = @Content, description = "Liked")
    @ApiResponse(responseCode = "404", content = @Content, description = "Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<LikeResponseDTO> likeComment(@PathVariable UUID commentId,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(commentService.like(commentId, user));
    }

    @DeleteMapping("/{commentId}/like")
    @ApiResponse(responseCode = "200", content = @Content, description = "UnLiked")
    @ApiResponse(responseCode = "404", content = @Content, description = "Not Found")
    @ApiResponse(responseCode = "400", content = @Content, description = "Invalid ID")
    public ResponseEntity<LikeResponseDTO> unLike(@PathVariable UUID commentId,
                                                  @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(commentService.unLike(commentId, user));
    }

    @PostMapping("/{commentId}/reply")
    @ApiResponse(responseCode = "201", description = "Reply created")
    @ApiResponse(responseCode = "409", description = "Comment already has a reply")
    @ApiResponse(responseCode = "403", description = "Not authorized")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<CommentReplyResponseDTO> reply(
            @PathVariable UUID commentId,
            @RequestBody @Valid CommentReplyRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.reply(commentId, dto.content(), user));
    }

    @GetMapping("/{commentId}/reply")
    @ApiResponse(responseCode = "200", description = "Replies retrieved")
    public ResponseEntity<CommentReplyResponseDTO> getReply(
            @PathVariable UUID commentId){
        return ResponseEntity.ok(commentService.getByReplyByCommentId(commentId));
    }

    @PutMapping("/{commentId}/reply")
    @ApiResponse(responseCode = "200", description = "Reply updated")
    @ApiResponse(responseCode = "403", description = "Not authorized")
    @ApiResponse(responseCode = "404", description = "Reply not found")
    public ResponseEntity<CommentReplyResponseDTO> updateReply(
            @PathVariable UUID commentId,
            @RequestBody @Valid CommentReplyRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(commentService.updateReply(commentId, dto.content(), user));
    }

    @DeleteMapping("/{commentId}/reply/{replyId}")
    @ApiResponse(responseCode = "204", description = "Reply deleted")
    public ResponseEntity<Void> deleteReply(
            @PathVariable UUID replyId,
            @AuthenticationPrincipal User user) {
        commentService.deleteReply(replyId, user);
        return ResponseEntity.noContent().build();
    }

}
