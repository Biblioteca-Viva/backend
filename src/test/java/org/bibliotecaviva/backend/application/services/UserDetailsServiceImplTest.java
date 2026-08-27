package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserDetailsServiceImpl service;

    @Test
    void loadUserByUsernameShouldReturnDomainUser() {
        User user = User.builder().name("User").email("user@test.com").password("hash")
                .role(Role.ALUNO).accountStatus(Status.ACTIVE).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertSame(user, service.loadUserByUsername(user.getEmail()));
    }

    @Test
    void loadUserByUsernameShouldFailWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@test.com"));
    }
}
