package org.bibliotecaviva.backend.api.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.response.AdminDashboardResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.CommentSummaryResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.ReviewSummaryResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.UserResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.imports.ImportUsersResponseDTO;
import org.bibliotecaviva.backend.application.services.*;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final BookClubReviewService bookClubReviewService;
    private final UsersImportService usersImportService;

    //todo: trocar role pra curador / deletar

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

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "204", description = "User permanently deleted successfully")
    @ApiResponse(responseCode = "404", description = "User Not Found")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
    public ResponseEntity<Page<CommentSummaryResponseDTO>> getAllComments(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(commentService.getAll(pageable));
    }

    @GetMapping("/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "200", description = "BookClub reviews retrieved successfully")
    public ResponseEntity<Page<ReviewSummaryResponseDTO>> getAllReviews(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookClubReviewService.getAll(pageable));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponseDTO> getDashboardData() {
        return ResponseEntity.ok(new AdminDashboardResponseDTO(
                workService.countWorks(),
                commentService.countComments(),
                bookClubReviewService.countReviews(),
                userManagementService.countUsers(),
                userManagementService.countPendingUsers()));
    }

    @PostMapping(value = "/users/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "200", description = "Users imported successfully")
    @ApiResponse(responseCode = "400", description = "Invalid spreadsheet")
    public ResponseEntity<ImportUsersResponseDTO> importUsers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(usersImportService.importUsers(file));
    }
}
