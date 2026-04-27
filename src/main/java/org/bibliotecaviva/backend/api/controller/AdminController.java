package org.bibliotecaviva.backend.api.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.response.AdminDashboardResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.CommentSummaryResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.UserResponseDTO;
import org.bibliotecaviva.backend.application.services.CommentService;
import org.bibliotecaviva.backend.application.services.UserManagementService;
import org.bibliotecaviva.backend.application.services.WorkService;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Controller responsible for handling admin operations such as retrieving user information,"
        +
        " activating, rejecting, and blocking users. " +
        "Only accessible by administrators.")
public class AdminController {

    private final UserManagementService userManagementService;
    private final CommentService commentService;
    private final WorkService workService;

    //todo: registrar conta de curador / trocar role pra curador / deletar

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Invalid status value.")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(required = false) Status status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userManagementService.getAllUsers(status, pageable));
    }

    @PatchMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "204", description = "User activated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid user ID or user is already active.")
    @ApiResponse(responseCode = "404", description = "User Not Found")
    public ResponseEntity<Void> activateUser(@PathVariable UUID id) {
        userManagementService.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "204", description = "User rejected successfully")
    @ApiResponse(responseCode = "400", description = "Invalid user ID or user status is not pending.")
    @ApiResponse(responseCode = "404", description = "User Not Found")
    public ResponseEntity<Void> rejectUser(@PathVariable UUID id) {
        userManagementService.rejectUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/block/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "204", description = "User activated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid user ID or user is already blocked.")
    @ApiResponse(responseCode = "404", description = "User Not Found")
    public ResponseEntity<Void> blockUser(@PathVariable UUID id) {
        userManagementService.blockUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    public ResponseEntity<Page<CommentSummaryResponseDTO>> getAllComments(@PageableDefault() Pageable pageable) {
        return ResponseEntity.ok(commentService.getAll(pageable));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponseDTO> getDashboardData() {
        return ResponseEntity.ok(new AdminDashboardResponseDTO(
                workService.countWorks(),
                commentService.countComments(),
                userManagementService.countUsers(),
                userManagementService.countPendingUsers()));
    }
}
