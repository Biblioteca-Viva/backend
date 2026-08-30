package org.bibliotecaviva.backend.application.services;

import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.LoginRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.RegisterRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.LoginResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.RegisterResponseDTO;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.TooManyRequestsException;
import org.bibliotecaviva.backend.domain.exceptions.UserAlreadyExistsException;
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiterService rateLimiterService;

    @Value("${security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled = true;

    @Value("${security.rate-limit.login-account-limit:5}")
    private int loginAccountLimit = 5;

    @Value("${security.rate-limit.window-millis:60000}")
    private long windowMillis = 60000;

    public record AuthResult(LoginResponseDTO responseDTO, ResponseCookie refreshCookie) {}

    public AuthResult login(LoginRequestDTO request) {
        if (rateLimitEnabled && request.email() != null) {
            String accountKey = "login:account:" + request.email().toLowerCase().trim();
            if (!rateLimiterService.isAllowed(accountKey, loginAccountLimit, windowMillis)) {
                long retryAfter = rateLimiterService.getRetryAfterSeconds(accountKey, windowMillis);
                throw new TooManyRequestsException(
                        "Muitas tentativas de login para esta conta. Por favor, aguarde " + retryAfter + " segundos."
                );
            }
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = (User) userDetailsService.loadUserByUsername(request.email());
        String accessToken = jwtService.generateToken(user);
        var refreshTokenData = refreshTokenService.createRefreshToken(user);
        ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(
                refreshTokenData.rawToken(),
                refreshTokenData.duration()
        );

        LoginResponseDTO responseDTO = new LoginResponseDTO(
                accessToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return new AuthResult(responseDTO, refreshCookie);
    }

    public AuthResult refresh(String rawRefreshToken) {
        var rotated = refreshTokenService.rotateRefreshToken(rawRefreshToken);
        User user = rotated.entity().getUser();
        String newAccessToken = jwtService.generateToken(user);
        ResponseCookie newRefreshCookie = cookieService.createRefreshTokenCookie(
                rotated.rawToken(),
                rotated.duration()
        );

        LoginResponseDTO responseDTO = new LoginResponseDTO(
                newAccessToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return new AuthResult(responseDTO, newRefreshCookie);
    }

    public ResponseCookie logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revokeToken(rawRefreshToken);
        }
        return cookieService.createCleanRefreshTokenCookie();
    }

    private RegisterResponseDTO createUser(RegisterRequestDTO request, Role role, Status status, String successMessage) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("O email inserido já está em uso");
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .accountStatus(status)
                .build();

        userRepository.save(user);
        return new RegisterResponseDTO(user.getName(), user.getEmail(), successMessage);
    }

    public RegisterResponseDTO registerCurador(RegisterRequestDTO request) {
        return createUser(request, Role.CURADOR, Status.ACTIVE, "Curador cadastrado com sucesso!");
    }

    public RegisterResponseDTO registerAluno(RegisterRequestDTO request) {
        return createUser(request, Role.ALUNO, Status.PENDING, "Pedido gerado com sucesso, aguarde a aprovação do administrador!");
    }

    public RegisterResponseDTO registerAdmin(RegisterRequestDTO request) {
        return createUser(request, Role.ADMIN, Status.ACTIVE, "Admin cadastrado com sucesso!");
    }
}
