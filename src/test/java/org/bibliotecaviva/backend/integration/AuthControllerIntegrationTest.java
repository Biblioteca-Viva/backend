package org.bibliotecaviva.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void registerAlunoShouldCreatePendingStudent() throws Exception {
        String email = uniqueEmail("registro");

        mockMvc.perform(post("/auth/register/aluno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Novo aluno",
                                "email", email,
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Novo aluno"))
                .andExpect(jsonPath("$.email").value(email));

        User saved = userRepository.findByEmail(email).orElseThrow();
        assertEquals(Role.ALUNO, saved.getRole());
        assertEquals(Status.PENDING, saved.getAccountStatus());
        assertFalse(passwordEncoder.matches("senha-errada", saved.getPassword()));
        assertTrue(passwordEncoder.matches(RAW_PASSWORD, saved.getPassword()));
    }

    @Test
    void registerAlunoShouldRejectDuplicatedEmail() throws Exception {
        User existing = createActiveStudent();

        mockMvc.perform(post("/auth/register/aluno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Outro aluno",
                                "email", existing.getEmail(),
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void adminShouldRegisterCuratorAndAdminAsActiveUsers() throws Exception {
        User admin = createActiveAdmin();
        String authorization = bearer(admin);
        String curatorEmail = uniqueEmail("novo-curador");
        String adminEmail = uniqueEmail("novo-admin");

        mockMvc.perform(post("/auth/register/curador")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Novo curador",
                                "email", curatorEmail,
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Novo curador"))
                .andExpect(jsonPath("$.email").value(curatorEmail))
                .andExpect(jsonPath("$.message").value("Curador cadastrado com sucesso!"));

        mockMvc.perform(post("/auth/register/admin")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Novo admin",
                                "email", adminEmail,
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Novo admin"))
                .andExpect(jsonPath("$.email").value(adminEmail))
                .andExpect(jsonPath("$.message").value("Admin cadastrado com sucesso!"));

        User savedCurator = userRepository.findByEmail(curatorEmail).orElseThrow();
        assertEquals(Role.CURADOR, savedCurator.getRole());
        assertEquals(Status.ACTIVE, savedCurator.getAccountStatus());
        assertTrue(passwordEncoder.matches(RAW_PASSWORD, savedCurator.getPassword()));

        User savedAdmin = userRepository.findByEmail(adminEmail).orElseThrow();
        assertEquals(Role.ADMIN, savedAdmin.getRole());
        assertEquals(Status.ACTIVE, savedAdmin.getAccountStatus());
        assertTrue(passwordEncoder.matches(RAW_PASSWORD, savedAdmin.getPassword()));
    }

    @Test
    void nonAdminShouldNotRegisterCuratorOrAdmin() throws Exception {
        User curator = createActiveCurator();
        String authorization = bearer(curator);

        mockMvc.perform(post("/auth/register/curador")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Curador recusado",
                                "email", uniqueEmail("curador-negado"),
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/auth/register/admin")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Admin recusado",
                                "email", uniqueEmail("admin-negado"),
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginShouldReturnAccessTokenAndSetRefreshTokenCookieForActiveUser() throws Exception {
        User user = createActiveStudent();

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", user.getEmail(),
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/auth"))
                .andReturn();

        JsonNode response = jsonFrom(result);
        String accessToken = response.get("accessToken").asText();
        assertEquals(user.getEmail(), jwtService.extractUsername(accessToken));
    }

    @Test
    void loginShouldRejectPendingUser() throws Exception {
        User user = createPendingStudent();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", user.getEmail(),
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void refreshShouldRotateRefreshTokenAndIssueNewAccessToken() throws Exception {
        User user = createActiveStudent();

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", user.getEmail(),
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertNotNull(refreshCookie);

        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        Cookie newRefreshCookie = refreshResult.getResponse().getCookie("refreshToken");
        assertNotNull(newRefreshCookie);
        assertNotEquals(refreshCookie.getValue(), newRefreshCookie.getValue());
    }

    @Test
    void refreshShouldFailWithInvalidOrMissingCookie() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refreshToken", "invalid-token-123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshShouldDetectReplayAttackAndRevokeTokens() throws Exception {
        User user = createActiveStudent();

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", user.getEmail(),
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        Cookie originalCookie = loginResult.getResponse().getCookie("refreshToken");
        assertNotNull(originalCookie);

        // First rotation succeeds
        mockMvc.perform(post("/auth/refresh").cookie(originalCookie))
                .andExpect(status().isOk());

        // Second rotation with the old token triggers reuse detection
        mockMvc.perform(post("/auth/refresh").cookie(originalCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutShouldRevokeCookieAndClearClientState() throws Exception {
        User user = createActiveStudent();

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", user.getEmail(),
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertNotNull(refreshCookie);

        mockMvc.perform(post("/auth/logout").cookie(refreshCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @Test
    void getCurrentUserShouldReturnProfileWhenAuthenticated() throws Exception {
        User user = createActiveStudent();

        mockMvc.perform(get("/auth/me").header("Authorization", bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.name").value(user.getName()));
    }

    @Test
    void getCurrentUserShouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isForbidden());
    }
}
