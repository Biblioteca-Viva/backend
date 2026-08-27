package org.bibliotecaviva.backend.application.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CookieServiceTest {

    private CookieService cookieService;

    @BeforeEach
    void setUp() {
        cookieService = new CookieService();
        ReflectionTestUtils.setField(cookieService, "secure", false);
        ReflectionTestUtils.setField(cookieService, "sameSite", "Lax");
    }

    @Test
    void createRefreshTokenCookieShouldSetCorrectAttributes() {
        ResponseCookie cookie = cookieService.createRefreshTokenCookie("my-refresh-token", Duration.ofDays(7));

        assertEquals("refreshToken", cookie.getName());
        assertEquals("my-refresh-token", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.isSecure());
        assertEquals("/auth", cookie.getPath());
        assertEquals(Duration.ofDays(7), cookie.getMaxAge());
        assertEquals("Lax", cookie.getSameSite());
    }

    @Test
    void createCleanRefreshTokenCookieShouldHaveZeroMaxAge() {
        ResponseCookie cookie = cookieService.createCleanRefreshTokenCookie();

        assertEquals("refreshToken", cookie.getName());
        assertEquals("", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertEquals(Duration.ZERO, cookie.getMaxAge());
        assertEquals("/auth", cookie.getPath());
    }

    @Test
    void extractRefreshTokenFromRequestShouldReturnTokenWhenPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie[] cookies = new Cookie[]{
                new Cookie("other", "value"),
                new Cookie("refreshToken", "token123")
        };
        when(request.getCookies()).thenReturn(cookies);

        Optional<String> token = cookieService.extractRefreshTokenFromRequest(request);

        assertTrue(token.isPresent());
        assertEquals("token123", token.get());
    }

    @Test
    void extractRefreshTokenFromRequestShouldReturnEmptyWhenNoCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        Optional<String> token = cookieService.extractRefreshTokenFromRequest(request);

        assertTrue(token.isEmpty());
    }
}
