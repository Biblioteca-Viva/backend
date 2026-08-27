package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.application.dtos.request.LoginRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.RegisterRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.LoginResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.RegisterResponseDTO;
import org.bibliotecaviva.backend.domain.entities.RefreshToken;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.UserAlreadyExistsException;
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private CookieService cookieService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RateLimiterService rateLimiterService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldAuthenticateLoadUserAndReturnAuthResult() {
        LoginRequestDTO request = new LoginRequestDTO("admin@teste.com", "123456");
        User user = buildUser("admin", request.email(), Role.ADMIN, Status.ACTIVE);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "raw-token").build();

        when(rateLimiterService.isAllowed(any(), anyInt(), anyLong())).thenReturn(true);
        when(userDetailsService.loadUserByUsername(request.email())).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(
                new RefreshTokenService.GeneratedRefreshToken("raw-token", RefreshToken.builder().user(user).build(), Duration.ofDays(7))
        );
        when(cookieService.createRefreshTokenCookie(eq("raw-token"), any(Duration.class))).thenReturn(cookie);

        AuthService.AuthResult result = authService.login(request);

        assertNotNull(result);
        LoginResponseDTO response = result.responseDTO();
        assertEquals("jwt-token", response.accessToken());
        assertEquals(request.email(), response.email());
        assertEquals(user.getName(), response.name());
        assertEquals(user.getId(), response.id());
        assertEquals(Role.ADMIN, response.role());
        assertEquals(cookie, result.refreshCookie());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        assertEquals(request.email(), authCaptor.getValue().getPrincipal());
        assertEquals(request.password(), authCaptor.getValue().getCredentials());
        verify(userDetailsService).loadUserByUsername(request.email());
        verify(jwtService).generateToken(user);
    }

    @Test
    void loginShouldThrowTooManyRequestsExceptionWhenAccountLimitExceeded() {
        LoginRequestDTO request = new LoginRequestDTO("aluno@teste.com", "123456");
        when(rateLimiterService.isAllowed(eq("login:account:aluno@teste.com"), anyInt(), anyLong())).thenReturn(false);
        when(rateLimiterService.getRetryAfterSeconds(eq("login:account:aluno@teste.com"), anyLong())).thenReturn(45L);

        assertThrows(org.bibliotecaviva.backend.domain.exceptions.TooManyRequestsException.class, () -> authService.login(request));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void refreshShouldRotateTokenAndReturnNewAccessTokenAndCookie() {
        User user = buildUser("admin", "admin@teste.com", Role.ADMIN, Status.ACTIVE);
        ResponseCookie newCookie = ResponseCookie.from("refreshToken", "new-raw-token").build();

        when(refreshTokenService.rotateRefreshToken("old-raw-token")).thenReturn(
                new RefreshTokenService.GeneratedRefreshToken("new-raw-token", RefreshToken.builder().user(user).build(), Duration.ofDays(7))
        );
        when(jwtService.generateToken(user)).thenReturn("new-jwt-token");
        when(cookieService.createRefreshTokenCookie(eq("new-raw-token"), any(Duration.class))).thenReturn(newCookie);

        AuthService.AuthResult result = authService.refresh("old-raw-token");

        assertNotNull(result);
        LoginResponseDTO response = result.responseDTO();
        assertEquals("new-jwt-token", response.accessToken());
        assertEquals(user.getEmail(), response.email());
        assertEquals(newCookie, result.refreshCookie());
        verify(jwtService).generateToken(user);
    }

    @Test
    void logoutShouldRevokeTokenAndReturnCleanCookie() {
        ResponseCookie cleanCookie = ResponseCookie.from("refreshToken", "").maxAge(0).build();
        when(cookieService.createCleanRefreshTokenCookie()).thenReturn(cleanCookie);

        ResponseCookie result = authService.logout("raw-token");

        assertEquals(cleanCookie, result);
        verify(refreshTokenService).revokeToken("raw-token");
        verify(cookieService).createCleanRefreshTokenCookie();
    }

    @Test
    void registerAlunoShouldCreatePendingStudentWithEncodedPassword() {
        RegisterRequestDTO request = new RegisterRequestDTO("Aluno", "aluno@teste.com", "123456");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        RegisterResponseDTO response = authService.registerAluno(request);

        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());
        assertTrue(response.message().startsWith("Pedido gerado com sucesso"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(request.name(), saved.getName());
        assertEquals(request.email(), saved.getEmail());
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(Role.ALUNO, saved.getRole());
        assertEquals(Status.PENDING, saved.getAccountStatus());
    }

    @Test
    void registerCuradorShouldCreateActiveCuratorWithEncodedPassword() {
        RegisterRequestDTO request = new RegisterRequestDTO("Curador", "curador@teste.com", "123456");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        RegisterResponseDTO response = authService.registerCurador(request);

        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());
        assertEquals("Curador cadastrado com sucesso!", response.message());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(request.name(), saved.getName());
        assertEquals(request.email(), saved.getEmail());
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(Role.CURADOR, saved.getRole());
        assertEquals(Status.ACTIVE, saved.getAccountStatus());
    }

    @Test
    void registerAdminShouldCreateActiveAdminWithEncodedPassword() {
        RegisterRequestDTO request = new RegisterRequestDTO("Admin", "admin@teste.com", "123456");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        RegisterResponseDTO response = authService.registerAdmin(request);

        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());
        assertEquals("Admin cadastrado com sucesso!", response.message());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(request.name(), saved.getName());
        assertEquals(request.email(), saved.getEmail());
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(Role.ADMIN, saved.getRole());
        assertEquals(Status.ACTIVE, saved.getAccountStatus());
    }

    @Test
    void registerAlunoShouldFailWhenEmailAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO("Aluno", "aluno@teste.com", "123456");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerAluno(request));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    private static User buildUser(String name, String email, Role role, Status status) {
        return User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .password("123456")
                .role(role)
                .accountStatus(status)
                .build();
    }
}
