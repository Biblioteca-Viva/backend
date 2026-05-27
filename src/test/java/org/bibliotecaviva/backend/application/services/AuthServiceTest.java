package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.application.dtos.request.LoginRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.RegisterRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.LoginResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.RegisterResponseDTO;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.UserAlreadyExistsException;
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldAuthenticateLoadUserAndReturnGeneratedToken() {
        LoginRequestDTO request = new LoginRequestDTO("admin@teste.com", "123456");
        User user = buildUser("admin", request.email(), Role.ADMIN, Status.ACTIVE);

        when(userDetailsService.loadUserByUsername(request.email())).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponseDTO response = authService.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals(request.email(), response.email());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        assertEquals(request.email(), authCaptor.getValue().getPrincipal());
        assertEquals(request.password(), authCaptor.getValue().getCredentials());
        verify(userDetailsService).loadUserByUsername(request.email());
        verify(jwtService).generateToken(user);
    }

    @Test
    void registerAlunoShouldCreatePendingStudentWithEncodedPassword() {
        RegisterRequestDTO request = new RegisterRequestDTO("Aluno", "aluno@teste.com", "123456");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        RegisterResponseDTO response = authService.registerAluno(request);

        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());
        assertTrue(response.message().startsWith("Pedido gerado com sucesso"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(request.name(), saved.getName());
        assertEquals(request.email(), saved.getEmail());
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(Role.ALUNO, saved.getRole());
        assertEquals(Status.PENDING, saved.getAccountStatus());
    }

    @Test
    void registerCuradorShouldCreateActiveCuratorWithEncodedPassword() {
        RegisterRequestDTO request = new RegisterRequestDTO("Curador", "curador@teste.com", "123456");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        RegisterResponseDTO response = authService.registerCurador(request);

        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());
        assertEquals("Curador cadastrado com sucesso!", response.message());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(request.name(), saved.getName());
        assertEquals(request.email(), saved.getEmail());
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(Role.CURADOR, saved.getRole());
        assertEquals(Status.ACTIVE, saved.getAccountStatus());
    }

    @Test
    void registerAdminShouldCreateActiveAdminWithEncodedPassword() {
        RegisterRequestDTO request = new RegisterRequestDTO("Admin", "admin@teste.com", "123456");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        RegisterResponseDTO response = authService.registerAdmin(request);

        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());
        assertEquals("Admin cadastrado com sucesso!", response.message());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals(request.name(), saved.getName());
        assertEquals(request.email(), saved.getEmail());
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(Role.ADMIN, saved.getRole());
        assertEquals(Status.ACTIVE, saved.getAccountStatus());
    }

    @Test
    void registerAlunoShouldFailWhenEmailAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO("Aluno", "aluno@teste.com", "123456");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerAluno(request));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    private static User buildUser(String name, String email, Role role, Status status) {
        return User.builder()
                .name(name)
                .email(email)
                .password("123456")
                .role(role)
                .accountStatus(status)
                .build();
    }
}
