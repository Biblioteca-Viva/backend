package org.bibliotecaviva.backend.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dto.LoginRequestDTO;
import org.bibliotecaviva.backend.application.dto.LoginResponseDTO;
import org.bibliotecaviva.backend.application.dto.RegisterRequestDTO;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.infrastructure.persistance.repository.UserRepository;
import org.bibliotecaviva.backend.domain.entities.user.User;
import org.bibliotecaviva.backend.infrastructure.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = (User) userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponseDTO(token, user.getEmail(), user.getRole()));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        if (request.role() == Role.VISITANTE) {
            return ResponseEntity.badRequest().build();
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponseDTO(token, user.getEmail(), user.getRole()));
    }
}
