package org.bibliotecaviva.backend.integration;

import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void adminShouldFindUserByEmail() throws Exception {
        User admin = createActiveAdmin();
        User target = createActiveStudent();

        mockMvc.perform(get("/user/find-by-email")
                        .header("Authorization", bearer(admin))
                        .queryParam("email", target.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(target.getId().toString()))
                .andExpect(jsonPath("$.name").value(target.getName()))
                .andExpect(jsonPath("$.email").value(target.getEmail()))
                .andExpect(jsonPath("$.role").value(Role.ALUNO.name()))
                .andExpect(jsonPath("$.accountStatus").value("active"));
    }

    @Test
    void curatorShouldFindUserByEmail() throws Exception {
        User curator = createActiveCurator();
        User target = createUser("Admin buscado", uniqueEmail("admin-buscado"), Role.ADMIN, Status.ACTIVE);

        mockMvc.perform(get("/user/find-by-email")
                        .header("Authorization", bearer(curator))
                        .queryParam("email", target.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(target.getId().toString()))
                .andExpect(jsonPath("$.email").value(target.getEmail()))
                .andExpect(jsonPath("$.role").value(Role.ADMIN.name()))
                .andExpect(jsonPath("$.accountStatus").value("active"));
    }

    @Test
    void findUserByEmailShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        User admin = createActiveAdmin();

        mockMvc.perform(get("/user/find-by-email")
                        .header("Authorization", bearer(admin))
                        .queryParam("email", uniqueEmail("ausente")))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentShouldNotFindUserByEmail() throws Exception {
        User student = createActiveStudent();
        User target = createActiveCurator();

        mockMvc.perform(get("/user/find-by-email")
                        .header("Authorization", bearer(student))
                        .queryParam("email", target.getEmail()))
                .andExpect(status().isForbidden());
    }
}
