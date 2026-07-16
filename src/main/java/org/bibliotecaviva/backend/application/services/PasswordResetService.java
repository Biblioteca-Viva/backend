package org.bibliotecaviva.backend.application.services;

import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.PasswordResetConfirmDTO;
import org.bibliotecaviva.backend.application.dtos.request.PasswordResetRequestDTO;
import org.bibliotecaviva.backend.application.dtos.request.PasswordResetVerifyDTO;
import org.bibliotecaviva.backend.application.dtos.response.PasswordResetVerifyResponseDTO;
import org.bibliotecaviva.backend.domain.entities.PasswordResetChallenge;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.InvalidPasswordResetException;
import org.bibliotecaviva.backend.persistence.repository.PasswordResetChallengeRepository;
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(10);
    private static final Duration RESET_TOKEN_EXPIRATION = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int RESET_TOKEN_BYTES = 32;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final UserRepository userRepository;
    private final PasswordResetChallengeRepository challengeRepository;
    private final ResendEmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${password-reset.pepper}")
    private String pepper;

    @Transactional
    public void requestCode(PasswordResetRequestDTO request) {
        User user = userRepository.findByEmailAndStatusForUpdate(request.email(), Status.ACTIVE)
                .orElse(null);
        if (user == null) {
            return;
        }

        Instant now = Instant.now();
        PasswordResetChallenge challenge = challengeRepository.findByUserIdForUpdate(user.getId())
                .orElse(null);

        if (challenge != null && now.isBefore(challenge.getLastSentAt().plus(RESEND_COOLDOWN))) {
            return;
        }

        String code = generateCode();
        if (challenge == null) {
            challenge = PasswordResetChallenge.builder().user(user).build();
        }

        challenge.setCodeHash(hash(code));
        challenge.setCodeExpiresAt(now.plus(CODE_EXPIRATION));
        challenge.setFailedAttempts(0);
        challenge.setLastSentAt(now);
        challenge.setResetTokenHash(null);
        challenge.setResetTokenExpiresAt(null);
        challenge.setVerified(false);

        challengeRepository.saveAndFlush(challenge);
        emailService.sendPasswordResetCode(user.getEmail(), code);
    }

    @Transactional(noRollbackFor = InvalidPasswordResetException.class)
    public PasswordResetVerifyResponseDTO verifyCode(PasswordResetVerifyDTO request) {
        User user = userRepository.findByEmailAndStatusForUpdate(request.email(), Status.ACTIVE)
                .orElseThrow(InvalidPasswordResetException::new);
        PasswordResetChallenge challenge = challengeRepository.findByUserIdForUpdate(user.getId())
                .orElseThrow(InvalidPasswordResetException::new);
        Instant now = Instant.now();

        if (challenge.isVerified()
                || challenge.getFailedAttempts() >= MAX_FAILED_ATTEMPTS
                || !now.isBefore(challenge.getCodeExpiresAt())) {
            throw new InvalidPasswordResetException();
        }

        if (!matches(request.code(), challenge.getCodeHash())) {
            challenge.setFailedAttempts(challenge.getFailedAttempts() + 1);
            challengeRepository.save(challenge);
            throw new InvalidPasswordResetException();
        }

        String resetToken = generateResetToken();
        challenge.setVerified(true);
        challenge.setResetTokenHash(hash(resetToken));
        challenge.setResetTokenExpiresAt(now.plus(RESET_TOKEN_EXPIRATION));
        challengeRepository.save(challenge);

        return new PasswordResetVerifyResponseDTO(resetToken, RESET_TOKEN_EXPIRATION.toSeconds());
    }

    @Transactional
    public void confirmReset(PasswordResetConfirmDTO request) {
        String resetTokenHash = hash(request.resetToken());
        var userId = challengeRepository.findUserIdByResetTokenHash(resetTokenHash)
                .orElseThrow(InvalidPasswordResetException::new);
        User user = userRepository.findByIdForUpdate(userId)
                .filter(candidate -> candidate.getAccountStatus() == Status.ACTIVE)
                .orElseThrow(InvalidPasswordResetException::new);
        PasswordResetChallenge challenge = challengeRepository.findByUserIdForUpdate(userId)
                .orElseThrow(InvalidPasswordResetException::new);
        Instant now = Instant.now();

        if (!challenge.isVerified()
                || challenge.getResetTokenHash() == null
                || challenge.getResetTokenExpiresAt() == null
                || !matches(request.resetToken(), challenge.getResetTokenHash())
                || !now.isBefore(challenge.getResetTokenExpiresAt())) {
            throw new InvalidPasswordResetException();
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setSessionVersion(user.getSessionVersion() + 1);
        userRepository.save(user);
        challengeRepository.delete(challenge);
    }

    private String generateCode() {
        return String.format(Locale.ROOT, "%08d", secureRandom.nextInt(100_000_000));
    }

    private String generateResetToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean matches(String rawValue, String expectedHash) {
        byte[] actual = hash(rawValue).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(actual, expected);
    }

    private String hash(String rawValue) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return java.util.HexFormat.of().formatHex(mac.doFinal(rawValue.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Não foi possível proteger a credencial de redefinição", exception);
        }
    }
}
