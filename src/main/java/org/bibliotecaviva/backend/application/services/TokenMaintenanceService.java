package org.bibliotecaviva.backend.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bibliotecaviva.backend.persistence.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;


@Service
@RequiredArgsConstructor
@Log4j2
public class TokenMaintenanceService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RateLimiterService rateLimiterService;

    @Scheduled(fixedRate = 3600_000, initialDelay = 60_000)
    @Transactional
    public void cleanupExpiredData() {
        int deletedTokens = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        rateLimiterService.cleanup();
        if (deletedTokens > 0) {
            log.info("Manutenção: {} refresh tokens expirados removidos", deletedTokens);
        }
    }
}
