package org.bibliotecaviva.backend.application.services;

import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.domain.entities.RefreshToken;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.InvalidRefreshTokenException;
import org.bibliotecaviva.backend.persistence.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${security.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpirationMillis;

    public record GeneratedRefreshToken(String rawToken, RefreshToken entity, Duration duration) {}

    @Transactional
    public GeneratedRefreshToken createRefreshToken(User user) {
        String rawToken = generateSecureRandomToken();
        String hash = hashToken(rawToken);
        Duration duration = Duration.ofMillis(refreshTokenExpirationMillis);
        Instant now = Instant.now();

        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .createdAt(now)
                .expiresAt(now.plus(duration))
                .revoked(false)
                .familyId(UUID.randomUUID())
                .build();

        RefreshToken saved = refreshTokenRepository.save(entity);
        return new GeneratedRefreshToken(rawToken, saved, duration);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public GeneratedRefreshToken rotateRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token não fornecido");
        }

        String hash = hashToken(rawToken);
        RefreshToken existingToken = refreshTokenRepository.findByTokenHashForUpdate(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido ou não encontrado"));

        User user = existingToken.getUser();

        if (existingToken.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(user.getId());
            throw new InvalidRefreshTokenException("Tentativa de reutilização de sessão detectada. Todas as sessões foram encerradas.");
        }

        Instant now = Instant.now();
        if (existingToken.getExpiresAt().isBefore(now)) {
            existingToken.setRevoked(true);
            refreshTokenRepository.save(existingToken);
            throw new InvalidRefreshTokenException("Refresh token expirado");
        }

        if (user.getAccountStatus() != Status.ACTIVE) {
            existingToken.setRevoked(true);
            refreshTokenRepository.save(existingToken);
            throw new InvalidRefreshTokenException("Conta inativa ou bloqueada");
        }

        String newRawToken = generateSecureRandomToken();
        String newHash = hashToken(newRawToken);
        Duration duration = Duration.ofMillis(refreshTokenExpirationMillis);

        existingToken.setRevoked(true);
        existingToken.setReplacedByTokenHash(newHash);
        refreshTokenRepository.save(existingToken);

        RefreshToken newTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(newHash)
                .createdAt(now)
                .expiresAt(now.plus(duration))
                .revoked(false)
                .familyId(existingToken.getFamilyId())
                .build();

        RefreshToken saved = refreshTokenRepository.save(newTokenEntity);
        return new GeneratedRefreshToken(newRawToken, saved, duration);
    }

    @Transactional
    public void revokeToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        String hash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private String generateSecureRandomToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash SHA-256 indisponível", e);
        }
    }
}
