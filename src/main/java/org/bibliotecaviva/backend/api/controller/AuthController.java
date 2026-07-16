package org.bibliotecaviva.backend.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.LoginRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.PasswordResetConfirmDTO;
import org.bibliotecaviva.backend.application.dtos.request.PasswordResetRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.PasswordResetVerifyDTO;
import org.bibliotecaviva.backend.application.dtos.request.RegisterRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.LoginResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.MessageResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.PasswordResetVerifyResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.RegisterResponseDTO;
import org.bibliotecaviva.backend.application.services.AuthService;
import org.bibliotecaviva.backend.application.services.JwtService;
import org.bibliotecaviva.backend.application.services.PasswordResetService;
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

    //todo: refresh-token, mudar para cookies e validar refresh no banco.

    private final AuthService authService;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    private static final String PASSWORD_RESET_REQUEST_MESSAGE =
            "Se o email pertencer a uma conta ativa, enviaremos um codigo de redefinicao.";

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
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/request")
    @Operation(description = "Envia um codigo de redefinicao para uma conta ativa")
    @ApiResponse(responseCode = "202", description = "Solicitacao processada")
    @ApiResponse(responseCode = "502", description = "Falha no provedor de email", content = @Content)
    public ResponseEntity<MessageResponseDTO> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDTO request
    ) {
        passwordResetService.requestCode(request);
        return ResponseEntity.accepted()
                .body(new MessageResponseDTO(PASSWORD_RESET_REQUEST_MESSAGE));
    }

    @PostMapping("/password-reset/verify")
    @Operation(description = "Valida o codigo e emite um token temporario de redefinicao")
    @ApiResponse(responseCode = "200", description = "Codigo validado")
    @ApiResponse(responseCode = "400", description = "Codigo invalido ou expirado", content = @Content)
    public ResponseEntity<PasswordResetVerifyResponseDTO> verifyPasswordResetCode(
            @Valid @RequestBody PasswordResetVerifyDTO request
    ) {
        return ResponseEntity.ok(passwordResetService.verifyCode(request));
    }

    @PostMapping("/password-reset/confirm")
    @Operation(description = "Define a nova senha usando o token temporario")
    @ApiResponse(responseCode = "204", description = "Senha redefinida")
    @ApiResponse(responseCode = "400", description = "Token invalido ou expirado", content = @Content)
    public ResponseEntity<Void> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmDTO request
    ) {
        passwordResetService.confirmReset(request);
        return ResponseEntity.noContent().build();
    }

}
