package org.bibliotecaviva.backend.application.services;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "Y29kZXgtdGVzdC1qd3Qtc2VjcmV0LWZvci1qd3Qtc2VydmljZS10ZXN0cw==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86_400_000L);
    }

    @Test
    void generateTokenShouldUseUserEmailAndIncludeRoleAndSessionVersionClaims() {
        User user = buildUser("admin@teste.com", Role.ADMIN);
        user.setSessionVersion(7);

        String token = jwtService.generateToken(user);

        assertEquals(user.getEmail(), jwtService.extractUsername(token));
        assertEquals(Role.ADMIN.name(), jwtService.extractClaim(token, claims -> claims.get("role", String.class)));
        assertEquals(7L, ((Number) jwtService.extractClaim(token, claims -> claims.get("sessionVersion"))).longValue());
    }

    @Test
    void isTokenValidShouldReturnTrueForMatchingUserAndNonExpiredToken() {
        User user = buildUser("aluno@teste.com", Role.ALUNO);
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValidShouldReturnFalseWhenUsernameDoesNotMatch() {
        User user = buildUser("aluno@teste.com", Role.ALUNO);
        User otherUser = buildUser("outro@teste.com", Role.ALUNO);
        String token = jwtService.generateToken(user);

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void isTokenValidShouldReturnFalseWhenSessionVersionChanged() {
        User user = buildUser("aluno@teste.com", Role.ALUNO);
        user.setSessionVersion(2);
        String token = jwtService.generateToken(user);

        user.setSessionVersion(3);

        assertFalse(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValidShouldRejectUserDetailsThatIsNotDomainUser() {
        User user = buildUser("aluno@teste.com", Role.ALUNO);
        String token = jwtService.generateToken(user);
        var springUser = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()).password("password").roles("ALUNO").build();

        assertFalse(jwtService.isTokenValid(token, springUser));
    }

    @Test
    void legacyTokenWithoutSessionVersionShouldOnlyValidateVersionZeroUser() {
        User user = buildUser("legacy@teste.com", Role.ALUNO);
        String token = jwtService.generateToken(Map.of("role", Role.ALUNO.name()), user);

        assertTrue(jwtService.isTokenValid(token, user));
        user.setSessionVersion(1);
        assertFalse(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValidShouldFailWhenTokenIsExpired() {
        User user = buildUser("aluno@teste.com", Role.ALUNO);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        String token = jwtService.generateToken(user);

        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, user));
    }

    @Test
    void extractUsernameShouldFailForMalformedToken() {
        assertThrows(JwtException.class, () -> jwtService.extractUsername("not-a-jwt"));
    }

    private static User buildUser(String email, Role role) {
        return User.builder()
                .name("Usuario")
                .email(email)
                .password("123456")
                .role(role)
                .accountStatus(Status.ACTIVE)
                .build();
    }
}
