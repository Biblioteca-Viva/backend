package org.bibliotecaviva.backend.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.services.CookieService;
import org.bibliotecaviva.backend.application.services.RateLimiterService;
import org.bibliotecaviva.backend.persistence.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${security.rate-limit.login-ip-limit:150}")
    private int loginIpLimit;

    @Value("${security.rate-limit.refresh-token-limit:15}")
    private int refreshTokenLimit;

    @Value("${security.rate-limit.refresh-ip-limit:300}")
    private int refreshIpLimit;

    @Value("${security.rate-limit.register-ip-limit:60}")
    private int registerIpLimit;

    @Value("${security.rate-limit.password-reset-ip-limit:30}")
    private int passwordResetIpLimit;

    @Value("${security.rate-limit.window-millis:60000}")
    private long windowMillis;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equalsIgnoreCase(method)) {
            String clientIp = extractClientIp(request);

            if (uri.endsWith("/auth/login")) {
                if (!checkLimit("login:ip:" + clientIp, loginIpLimit, response)) return;
            } else if (uri.endsWith("/auth/refresh")) {
                // Rate limit por sessão (evita hammering na mesma sessão)
                // Usa family_id estável, que sobrevive à rotação do token single-use
                String rawCookie = extractRefreshTokenCookie(request);
                if (rawCookie != null && !rawCookie.isBlank()) {
                    String tokenHash = hashToken(rawCookie);
                    Optional<UUID> familyId = refreshTokenRepository.findFamilyIdByTokenHash(tokenHash);
                    String tokenKey = familyId
                            .map(id -> "refresh:family:" + id)
                            .orElse("refresh:unknown:" + tokenHash);
                    if (!checkLimit(tokenKey, refreshTokenLimit, response)) return;
                }
                // Rate limit volumétrico por IP (amplo para acomodar redes escolares compartilhadas via NAT)
                if (!checkLimit("refresh:ip:" + clientIp, refreshIpLimit, response)) return;
            } else if (uri.contains("/auth/password-reset")) {
                if (!checkLimit("password-reset:ip:" + clientIp, passwordResetIpLimit, response)) return;
            } else if (uri.contains("/auth/register")) {
                if (!checkLimit("register:ip:" + clientIp, registerIpLimit, response)) return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkLimit(String key, int maxRequests, HttpServletResponse response) throws IOException {
        if (!rateLimiterService.isAllowed(key, maxRequests, windowMillis)) {
            long retryAfterSeconds = rateLimiterService.getRetryAfterSeconds(key, windowMillis);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> body = Map.of(
                    "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                    "error", "Too Many Requests",
                    "message", "Limite de requisições excedido. Por favor, aguarde " + retryAfterSeconds + " segundos."
            );

            response.getWriter().write(objectMapper.writeValueAsString(body));
            return false;
        }
        return true;
    }

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (CookieService.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(token.hashCode());
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }
}
