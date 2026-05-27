package org.bibliotecaviva.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void loginShouldReturnTokenForActiveUser() throws Exception {
        User user = createActiveStudent();

        JsonNode response = jsonFrom(mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", user.getEmail(),
                                "password", RAW_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn());

        String token = response.get("token").asText();
        assertEquals(user.getEmail(), jwtService.extractUsername(token));
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
    void logoutShouldInvalidateTokenForProtectedEndpoints() throws Exception {
        User user = createActiveStudent();
        String authorization = bearer(user);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/work/liked")
                        .header("Authorization", authorization))
                .andExpect(status().isUnauthorized());
    }
}
