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
import org.bibliotecaviva.backend.application.dtos.response.UserProfileResponseDTO;
import org.bibliotecaviva.backend.application.services.AuthService;
import org.bibliotecaviva.backend.application.services.CookieService;
import org.bibliotecaviva.backend.application.services.PasswordResetService;
import org.bibliotecaviva.backend.domain.entities.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Controller responsible for handling authentication-related operations such as login, token refresh, registration, and logout.")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    private static final String PASSWORD_RESET_REQUEST_MESSAGE =
            "Se o email pertencer a uma conta ativa, enviaremos um codigo de redefinicao.";

    @PostMapping("/login")
    @Operation(description = "Autentica o usuário, emite accessToken no corpo e refreshToken em cookie HttpOnly")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Credenciais Inválidas", content = @Content)
    @ApiResponse(responseCode = "403", description = "Conta pendente ou bloqueada", content = @Content)
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthService.AuthResult result = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.refreshCookie().toString())
                .body(result.responseDTO());
    }

    @PostMapping("/refresh")
    @Operation(description = "Renova o accessToken utilizando o refreshToken enviado via cookie HttpOnly")
    @ApiResponse(responseCode = "200", description = "Token renovado com sucesso")
    @ApiResponse(responseCode = "401", description = "Refresh token ausente, inválido ou expirado", content = @Content)
    public ResponseEntity<LoginResponseDTO> refresh(
            @CookieValue(name = CookieService.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        AuthService.AuthResult result = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.refreshCookie().toString())
                .body(result.responseDTO());
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

    @PostMapping("/logout")
    @Operation(description = "Revoga o refreshToken no banco de dados e limpa o cookie HttpOnly")
    @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso", content = @Content)
    public ResponseEntity<Void> logout(
            @CookieValue(name = CookieService.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        ResponseCookie cleanCookie = authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(description = "Retorna os dados do usuário autenticado no momento")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    public ResponseEntity<UserProfileResponseDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        UserProfileResponseDTO response = new UserProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
        return ResponseEntity.ok(response);
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
