package org.bibliotecaviva.backend.api.controller;

import org.bibliotecaviva.backend.application.dtos.response.UserResponseDTO;
import org.bibliotecaviva.backend.application.services.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping("/find-by-email")
    @PreAuthorize("hasAnyRole('ADMIN','CURADOR')")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "400", description = "Invalid param")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@RequestParam @NotBlank @Email String email) {
        return userManagementService.findUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
