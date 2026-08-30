package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.domain.entities.RefreshToken;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.InvalidRefreshTokenException;
import org.bibliotecaviva.backend.persistence.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMillis", 604800000L);
    }

    @Test
    void createRefreshTokenShouldSaveEntityAndReturnRawToken() {
        User user = buildUser(UUID.randomUUID(), "Aluno", "aluno@teste.com", Status.ACTIVE);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = refreshTokenService.createRefreshToken(user);

        assertNotNull(result);
        assertNotNull(result.rawToken());
        assertFalse(result.rawToken().isBlank());
        assertEquals(Duration.ofMillis(604800000L), result.duration());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertFalse(saved.isRevoked());
        assertNotNull(saved.getTokenHash());
        assertNotNull(saved.getExpiresAt());
        assertNotNull(saved.getFamilyId(), "familyId deve ser gerado ao criar um novo token");
    }

    @Test
    void rotateRefreshTokenShouldRevokeOldAndCreateNewWhenValid() {
        User user = buildUser(UUID.randomUUID(), "Aluno", "aluno@teste.com", Status.ACTIVE);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var initial = refreshTokenService.createRefreshToken(user);
        String rawToken = initial.rawToken();
        RefreshToken existingToken = initial.entity();

        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(existingToken));

        var rotated = refreshTokenService.rotateRefreshToken(rawToken);

        assertNotNull(rotated);
        assertNotEquals(rawToken, rotated.rawToken());
        assertTrue(existingToken.isRevoked());
        assertNotNull(existingToken.getReplacedByTokenHash());
        assertFalse(rotated.entity().isRevoked());
        assertEquals(user, rotated.entity().getUser());
        assertNotNull(rotated.entity().getFamilyId(), "familyId deve ser propagado na rotação");
        assertEquals(existingToken.getFamilyId(), rotated.entity().getFamilyId(),
                "familyId do token rotacionado deve ser idêntico ao do token original");
    }

    @Test
    void rotateRefreshTokenShouldDetectReplayAttackAndRevokeAllSessions() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "Aluno", "aluno@teste.com", Status.ACTIVE);

        RefreshToken revokedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("some-hash")
                .revoked(true)
                .createdAt(Instant.now().minusSeconds(1000))
                .expiresAt(Instant.now().plusSeconds(1000))
                .build();

        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(revokedToken));

        InvalidRefreshTokenException ex = assertThrows(InvalidRefreshTokenException.class, () ->
                refreshTokenService.rotateRefreshToken("any-raw-token")
        );

        assertTrue(ex.getMessage().contains("reutilização de sessão"));
        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }

    @Test
    void rotateRefreshTokenShouldRejectExpiredToken() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "Aluno", "aluno@teste.com", Status.ACTIVE);

        RefreshToken expiredToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("some-hash")
                .revoked(false)
                .createdAt(Instant.now().minusSeconds(10000))
                .expiresAt(Instant.now().minusSeconds(100))
                .build();

        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(expiredToken));

        InvalidRefreshTokenException ex = assertThrows(InvalidRefreshTokenException.class, () ->
                refreshTokenService.rotateRefreshToken("expired-token")
        );

        assertTrue(ex.getMessage().contains("expirado"));
        assertTrue(expiredToken.isRevoked());
        verify(refreshTokenRepository).save(expiredToken);
    }

    @Test
    void rotateRefreshTokenShouldRejectInactiveUser() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "Aluno", "aluno@teste.com", Status.BLOCKED);

        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("some-hash")
                .revoked(false)
                .createdAt(Instant.now().minusSeconds(100))
                .expiresAt(Instant.now().plusSeconds(1000))
                .build();

        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));

        InvalidRefreshTokenException ex = assertThrows(InvalidRefreshTokenException.class, () ->
                refreshTokenService.rotateRefreshToken("some-token")
        );

        assertTrue(ex.getMessage().contains("inativa ou bloqueada"));
        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void rotateRefreshTokenShouldThrowWhenTokenNotFound() {
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class, () ->
                refreshTokenService.rotateRefreshToken("invalid-token")
        );
    }

    @Test
    void rotateRefreshTokenShouldThrowWhenTokenIsBlank() {
        assertThrows(InvalidRefreshTokenException.class, () ->
                refreshTokenService.rotateRefreshToken("   ")
        );
    }

    @Test
    void revokeTokenShouldMarkEntityAsRevoked() {
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .tokenHash("hash")
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        refreshTokenService.revokeToken("raw-token");

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void revokeAllUserTokensShouldCallRepository() {
        UUID userId = UUID.randomUUID();
        refreshTokenService.revokeAllUserTokens(userId);
        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }

    private User buildUser(UUID id, String name, String email, Status status) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .password("123456")
                .role(Role.ALUNO)
                .accountStatus(status)
                .build();
    }
}
