package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.application.dtos.request.PasswordResetConfirmDTO;
import org.bibliotecaviva.backend.application.dtos.request.PasswordResetRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.PasswordResetVerifyDTO;
import org.bibliotecaviva.backend.domain.entities.PasswordResetChallenge;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.EmailDeliveryException;
import org.bibliotecaviva.backend.domain.exceptions.InvalidPasswordResetException;
import org.bibliotecaviva.backend.persistence.repository.PasswordResetChallengeRepository;
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final String PEPPER = "unit-test-password-reset-pepper";

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetChallengeRepository challengeRepository;
    @Mock private ResendEmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenService refreshTokenService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(userRepository, challengeRepository, emailService, passwordEncoder, refreshTokenService);
        ReflectionTestUtils.setField(service, "pepper", PEPPER);
    }

    @Test
    void requestCodeShouldCreateProtectedChallengeAndSendEightDigitCode() {
        User user = activeUser();
        when(userRepository.findByEmailAndStatusForUpdate(user.getEmail(), Status.ACTIVE)).thenReturn(Optional.of(user));
        when(challengeRepository.findByUserIdForUpdate(user.getId())).thenReturn(Optional.empty());
        Instant before = Instant.now();

        service.requestCode(new PasswordResetRequestDTO(user.getEmail()));

        ArgumentCaptor<PasswordResetChallenge> challengeCaptor = ArgumentCaptor.forClass(PasswordResetChallenge.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(challengeRepository).saveAndFlush(challengeCaptor.capture());
        verify(emailService).sendPasswordResetCode(eq(user.getEmail()), codeCaptor.capture());
        PasswordResetChallenge challenge = challengeCaptor.getValue();
        String code = codeCaptor.getValue();
        assertTrue(code.matches("\\d{8}"));
        assertEquals(hmac(code), challenge.getCodeHash());
        assertNotEquals(code, challenge.getCodeHash());
        assertTrue(challenge.getCodeHash().matches("[0-9a-f]{64}"));
        assertSame(user, challenge.getUser());
        assertEquals(0, challenge.getFailedAttempts());
        assertFalse(challenge.isVerified());
        assertNull(challenge.getResetTokenHash());
        assertNull(challenge.getResetTokenExpiresAt());
        assertFalse(challenge.getLastSentAt().isBefore(before));
        assertTrue(challenge.getCodeExpiresAt().isAfter(before.plusSeconds(590)));
        assertTrue(challenge.getCodeExpiresAt().isBefore(Instant.now().plusSeconds(610)));
    }

    @Test
    void requestCodeShouldSilentlyIgnoreUnknownOrInactiveAccount() {
        String email = "unknown@test.com";
        when(userRepository.findByEmailAndStatusForUpdate(email, Status.ACTIVE)).thenReturn(Optional.empty());

        service.requestCode(new PasswordResetRequestDTO(email));

        verifyNoInteractions(challengeRepository, emailService, passwordEncoder);
    }

    @Test
    void requestCodeShouldRespectResendCooldown() {
        User user = activeUser();
        PasswordResetChallenge challenge = challenge(user, "12345678");
        challenge.setLastSentAt(Instant.now().minusSeconds(30));
        when(userRepository.findByEmailAndStatusForUpdate(user.getEmail(), Status.ACTIVE)).thenReturn(Optional.of(user));
        when(challengeRepository.findByUserIdForUpdate(user.getId())).thenReturn(Optional.of(challenge));

        service.requestCode(new PasswordResetRequestDTO(user.getEmail()));

        verify(challengeRepository, never()).saveAndFlush(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void requestCodeShouldRenewChallengeAfterCooldown() {
        User user = activeUser();
        PasswordResetChallenge challenge = challenge(user, "12345678");
        String oldHash = challenge.getCodeHash();
        challenge.setLastSentAt(Instant.now().minusSeconds(61));
        challenge.setFailedAttempts(4);
        challenge.setVerified(true);
        challenge.setResetTokenHash("old-token-hash");
        challenge.setResetTokenExpiresAt(Instant.now().plusSeconds(30));
        when(userRepository.findByEmailAndStatusForUpdate(user.getEmail(), Status.ACTIVE)).thenReturn(Optional.of(user));
        when(challengeRepository.findByUserIdForUpdate(user.getId())).thenReturn(Optional.of(challenge));

        service.requestCode(new PasswordResetRequestDTO(user.getEmail()));

        verify(challengeRepository).saveAndFlush(challenge);
        verify(emailService).sendPasswordResetCode(eq(user.getEmail()), matches("\\d{8}"));
        assertNotEquals(oldHash, challenge.getCodeHash());
        assertEquals(0, challenge.getFailedAttempts());
        assertFalse(challenge.isVerified());
        assertNull(challenge.getResetTokenHash());
        assertNull(challenge.getResetTokenExpiresAt());
    }

    @Test
    void requestCodeShouldPropagateEmailDeliveryFailure() {
        User user = activeUser();
        when(userRepository.findByEmailAndStatusForUpdate(user.getEmail(), Status.ACTIVE)).thenReturn(Optional.of(user));
        when(challengeRepository.findByUserIdForUpdate(user.getId())).thenReturn(Optional.empty());
        doThrow(new EmailDeliveryException()).when(emailService).sendPasswordResetCode(eq(user.getEmail()), any());

        assertThrows(EmailDeliveryException.class,
                () -> service.requestCode(new PasswordResetRequestDTO(user.getEmail())));
        verify(challengeRepository).saveAndFlush(any(PasswordResetChallenge.class));
    }

    @Test
    void verifyCodeShouldIssueTemporaryTokenAndMarkChallengeVerified() {
        User user = activeUser();
        PasswordResetChallenge challenge = challenge(user, "01234567");
        when(userRepository.findByEmailAndStatusForUpdate(user.getEmail(), Status.ACTIVE)).thenReturn(Optional.of(user));
        when(challengeRepository.findByUserIdForUpdate(user.getId())).thenReturn(Optional.of(challenge));

        var response = service.verifyCode(new PasswordResetVerifyDTO(user.getEmail(), "01234567"));

        assertEquals(300, response.expiresInSeconds());
        assertTrue(response.resetToken().matches("[A-Za-z0-9_-]{43}"));
        assertTrue(challenge.isVerified());
        assertEquals(hmac(response.resetToken()), challenge.getResetTokenHash());
        assertNotEquals(response.resetToken(), challenge.getResetTokenHash());
        assertTrue(challenge.getResetTokenExpiresAt().isAfter(Instant.now().plusSeconds(290)));
        verify(challengeRepository).save(challenge);
    }

    @Test
    void verifyCodeShouldIncrementAttemptsForWrongCode() {
        User user = activeUser();
        PasswordResetChallenge challenge = challenge(user, "01234567");
        challenge.setFailedAttempts(2);
        when(userRepository.findByEmailAndStatusForUpdate(user.getEmail(), Status.ACTIVE)).thenReturn(Optional.of(user));
        when(challengeRepository.findByUserIdForUpdate(user.getId())).thenReturn(Optional.of(challenge));

        assertThrows(InvalidPasswordResetException.class,
                () -> service.verifyCode(new PasswordResetVerifyDTO(user.getEmail(), "76543210")));

        assertEquals(3, challenge.getFailedAttempts());
        verify(challengeRepository).save(challenge);
    }

    @ParameterizedTest
    @ValueSource(strings = {"VERIFIED", "MAX_ATTEMPTS", "EXPIRED"})
    void verifyCodeShouldRejectUnavailableChallenge(String state) {
        User user = activeUser();
        PasswordResetChallenge challenge = challenge(user, "01234567");
        if (state.equals("VERIFIED")) challenge.setVerified(true);
        if (state.equals("MAX_ATTEMPTS")) challenge.setFailedAttempts(5);
        if (state.equals("EXPIRED")) challenge.setCodeExpiresAt(Instant.now().minusSeconds(1));
        when(userRepository.findByEmailAndStatusForUpdate(user.getEmail(), Status.ACTIVE)).thenReturn(Optional.of(user));
        when(challengeRepository.findByUserIdForUpdate(user.getId())).thenReturn(Optional.of(challenge));

        assertThrows(InvalidPasswordResetException.class,
                () -> service.verifyCode(new PasswordResetVerifyDTO(user.getEmail(), "01234567")));
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void verifyCodeShouldRejectUnknownUserOrMissingChallenge() {
        String email = "missing@test.com";
        when(userRepository.findByEmailAndStatusForUpdate(email, Status.ACTIVE)).thenReturn(Optional.empty());
        assertThrows(InvalidPasswordResetException.class,
                () -> service.verifyCode(new PasswordResetVerifyDTO(email, "01234567")));

        User user = activeUser();
        when(userRepository.findByEmailAndStatusForUpdate(user.getEmail(), Status.ACTIVE)).thenReturn(Optional.of(user));
        when(challengeRepository.findByUserIdForUpdate(user.getId())).thenReturn(Optional.empty());
        assertThrows(InvalidPasswordResetException.class,
                () -> service.verifyCode(new PasswordResetVerifyDTO(user.getEmail(), "01234567")));
    }

    @Test
    void confirmResetShouldChangePasswordIncrementSessionAndDeleteChallenge() {
        User user = activeUser();
        user.setPassword("old-hash");
        user.setSessionVersion(3);
        String token = "valid-reset-token";
        PasswordResetChallenge challenge = verifiedChallenge(user, token);
        when(challengeRepository.findUserIdByResetTokenHash(hmac(token))).thenReturn(Optional.of(user.getId()));
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(challengeRepository.findByUserIdForUpdate(user.getId())).thenReturn(Optional.of(challenge));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.confirmReset(new PasswordResetConfirmDTO(token, "new-password"));

        assertEquals("new-hash", user.getPassword());
        assertEquals(4, user.getSessionVersion());
        verify(userRepository).save(user);
        verify(refreshTokenService).revokeAllUserTokens(user.getId());
        verify(challengeRepository).delete(challenge);
    }

    @Test
    void confirmResetShouldRejectMissingTokenOrInactiveUserOrMissingChallenge() {
        String token = "invalid-token";
        when(challengeRepository.findUserIdByResetTokenHash(hmac(token))).thenReturn(Optional.empty());
        assertInvalidConfirm(token);

        User inactive = activeUser();
        inactive.setAccountStatus(Status.BLOCKED);
        when(challengeRepository.findUserIdByResetTokenHash(hmac(token))).thenReturn(Optional.of(inactive.getId()));
        when(userRepository.findByIdForUpdate(inactive.getId())).thenReturn(Optional.of(inactive));
        assertInvalidConfirm(token);

        User active = activeUser();
        when(challengeRepository.findUserIdByResetTokenHash(hmac(token))).thenReturn(Optional.of(active.getId()));
        when(userRepository.findByIdForUpdate(active.getId())).thenReturn(Optional.of(active));
        when(challengeRepository.findByUserIdForUpdate(active.getId())).thenReturn(Optional.empty());
        assertInvalidConfirm(token);
        verify(userRepository, never()).save(any());
        verify(challengeRepository, never()).delete(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNVERIFIED", "NULL_HASH", "NULL_EXPIRY", "MISMATCH", "EXPIRED"})
    void confirmResetShouldRejectInvalidChallengeStateWithoutMutatingUser(String state) {
        User user = activeUser();
        user.setPassword("old-hash");
        user.setSessionVersion(2);
        String token = "valid-reset-token";
        PasswordResetChallenge challenge = verifiedChallenge(user, token);
        if (state.equals("UNVERIFIED")) challenge.setVerified(false);
        if (state.equals("NULL_HASH")) challenge.setResetTokenHash(null);
        if (state.equals("NULL_EXPIRY")) challenge.setResetTokenExpiresAt(null);
        if (state.equals("MISMATCH")) challenge.setResetTokenHash(hmac("another-token"));
        if (state.equals("EXPIRED")) challenge.setResetTokenExpiresAt(Instant.now().minusSeconds(1));
        when(challengeRepository.findUserIdByResetTokenHash(hmac(token))).thenReturn(Optional.of(user.getId()));
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(challengeRepository.findByUserIdForUpdate(user.getId())).thenReturn(Optional.of(challenge));

        assertInvalidConfirm(token);

        assertEquals("old-hash", user.getPassword());
        assertEquals(2, user.getSessionVersion());
        verify(userRepository, never()).save(any());
        verify(challengeRepository, never()).delete(any());
    }

    private void assertInvalidConfirm(String token) {
        assertThrows(InvalidPasswordResetException.class,
                () -> service.confirmReset(new PasswordResetConfirmDTO(token, "new-password")));
    }

    private static User activeUser() {
        return User.builder().id(UUID.randomUUID()).name("User").email(UUID.randomUUID() + "@test.com")
                .password("hash").role(Role.ALUNO).accountStatus(Status.ACTIVE).build();
    }

    private static PasswordResetChallenge challenge(User user, String code) {
        return PasswordResetChallenge.builder().id(UUID.randomUUID()).user(user).codeHash(hmac(code))
                .codeExpiresAt(Instant.now().plusSeconds(600)).failedAttempts(0)
                .lastSentAt(Instant.now().minusSeconds(120)).verified(false).build();
    }

    private static PasswordResetChallenge verifiedChallenge(User user, String token) {
        PasswordResetChallenge challenge = challenge(user, "01234567");
        challenge.setVerified(true);
        challenge.setResetTokenHash(hmac(token));
        challenge.setResetTokenExpiresAt(Instant.now().plusSeconds(300));
        return challenge;
    }

    private static String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(PEPPER.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
