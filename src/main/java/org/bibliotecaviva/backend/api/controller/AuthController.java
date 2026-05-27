package org.bibliotecaviva.backend.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.LoginRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.RegisterRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.LoginResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.RegisterResponseDTO;
import org.bibliotecaviva.backend.application.services.AuthService;
import org.bibliotecaviva.backend.application.services.JwtService;
import org.bibliotecaviva.backend.application.services.TokenBlacklistService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Controller responsible for handling authentication-related operations such as login, registration, and logout.")
public class AuthController {

    //todo: password reset,forgot-password and refresh-token,
    // mudar para cookies e validar refresh no banco.

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Credenciais Inválidas", content = @Content)
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register/aluno")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "409", description = "Conflict, email already exists", content = @Content)
    public ResponseEntity<RegisterResponseDTO> registerAluno(@Valid @RequestBody RegisterRequestDTO request) {
        return new ResponseEntity<>(authService.registerAluno(request), HttpStatus.CREATED);
    }

    @PostMapping("/register/curador")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "409", description = "Conflict, email already exists", content = @Content)
    public ResponseEntity<RegisterResponseDTO> registerCurador(@Valid @RequestBody RegisterRequestDTO request) {
        return new ResponseEntity<>(authService.registerCurador(request), HttpStatus.CREATED);
    }

    @PostMapping("/register/admin")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "409", description = "Conflict, email already exists", content = @Content)
    public ResponseEntity<RegisterResponseDTO> registerAdmin(@Valid @RequestBody RegisterRequestDTO request) {
        return new ResponseEntity<>(authService.registerAdmin(request), HttpStatus.CREATED);
    }

    @ApiResponse(responseCode = "401", description = "No token to remove or invalid token.", content = @Content)
    @ApiResponse(responseCode = "204", description = "No valid token to remove.", content = @Content)
    @Operation(description = "Add token to blacklist, remove after expire")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authorization.substring(7);
        tokenBlacklistService.blacklist(token, jwtService.extractExpiration(token));
        return ResponseEntity.noContent().build();
    }

}
