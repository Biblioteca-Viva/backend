package org.bibliotecaviva.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.bibliotecaviva.backend.application.services.ResendEmailService;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.EmailDeliveryException;
import org.bibliotecaviva.backend.persistence.repository.PasswordResetChallengeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasswordResetIntegrationTest extends IntegrationTestSupport {

    @MockitoBean
    private ResendEmailService emailService;

    @Autowired
    private PasswordResetChallengeRepository challengeRepository;

    @Test
    void shouldCompletePasswordResetInvalidateOldCredentialsAndPreventReuse() throws Exception {
        User user = createActiveStudent();
        String oldBearer = bearer(user);
        String newPassword = "new-password-123";

        mockMvc.perform(post("/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", user.getEmail()))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(
                        "Se o email pertencer a uma conta ativa, enviaremos um codigo de redefinicao."));

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetCode(eq(user.getEmail()), codeCaptor.capture());
        String code = codeCaptor.getValue();
        assertTrue(code.matches("\\d{8}"));

        JsonNode verifyResponse = jsonFrom(mockMvc.perform(post("/auth/password-reset/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", user.getEmail(), "code", code))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresInSeconds").value(300))
                .andReturn());
        String resetToken = verifyResponse.get("resetToken").asText();

        mockMvc.perform(post("/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("resetToken", resetToken, "newPassword", newPassword))))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches(newPassword, updated.getPassword()));
        assertEquals(1L, updated.getSessionVersion());
        assertTrue(challengeRepository.findByUserIdForUpdate(user.getId()).isEmpty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", user.getEmail(), "password", RAW_PASSWORD))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", user.getEmail(), "password", newPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
        mockMvc.perform(get("/work/liked").header("Authorization", oldBearer))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("resetToken", resetToken, "newPassword", "another-password"))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/password-reset/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", user.getEmail(), "code", code))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestShouldNotRevealAccountStateAndShouldRespectCooldown() throws Exception {
        User active = createActiveStudent();
        User pending = createPendingStudent();
        User blocked = createUser("Blocked", uniqueEmail("blocked"), Role.ALUNO, Status.BLOCKED);
        String message = "Se o email pertencer a uma conta ativa, enviaremos um codigo de redefinicao.";

        for (String email : new String[]{active.getEmail(), active.getEmail(), pending.getEmail(),
                blocked.getEmail(), uniqueEmail("missing")}) {
            mockMvc.perform(post("/auth/password-reset/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("email", email))))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.message").value(message));
        }

        verify(emailService, times(1)).sendPasswordResetCode(eq(active.getEmail()), anyString());
        verifyNoMoreInteractions(emailService);
    }

    @Test
    void endpointsShouldValidatePayloadAndTranslateEmailFailure() throws Exception {
        mockMvc.perform(post("/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "invalid"))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/password-reset/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "user@test.com", "code", "123"))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("resetToken", "token", "newPassword", "123"))))
                .andExpect(status().isBadRequest());

        User active = createActiveStudent();
        doThrow(new EmailDeliveryException()).when(emailService)
                .sendPasswordResetCode(eq(active.getEmail()), anyString());
        mockMvc.perform(post("/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", active.getEmail()))))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }
}
