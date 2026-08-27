package org.bibliotecaviva.backend.application.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        String key = "test-ip-1";
        int limit = 3;
        long window = 60_000L;

        assertTrue(rateLimiterService.isAllowed(key, limit, window));
        assertTrue(rateLimiterService.isAllowed(key, limit, window));
        assertTrue(rateLimiterService.isAllowed(key, limit, window));
        assertFalse(rateLimiterService.isAllowed(key, limit, window));
    }

    @Test
    void shouldCalculateRetryAfterSeconds() {
        String key = "test-ip-2";
        int limit = 1;
        long window = 10_000L;

        assertTrue(rateLimiterService.isAllowed(key, limit, window));
        assertFalse(rateLimiterService.isAllowed(key, limit, window));

        long retryAfter = rateLimiterService.getRetryAfterSeconds(key, window);
        assertTrue(retryAfter > 0 && retryAfter <= 10);
    }

    @Test
    void shouldIsolateKeys() {
        String key1 = "ip-1";
        String key2 = "ip-2";

        assertTrue(rateLimiterService.isAllowed(key1, 1, 60_000L));
        assertFalse(rateLimiterService.isAllowed(key1, 1, 60_000L));

        assertTrue(rateLimiterService.isAllowed(key2, 1, 60_000L));
    }
}
