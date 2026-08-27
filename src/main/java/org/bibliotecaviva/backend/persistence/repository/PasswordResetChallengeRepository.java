package org.bibliotecaviva.backend.persistence.repository;

import jakarta.persistence.LockModeType;
import org.bibliotecaviva.backend.domain.entities.PasswordResetChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetChallengeRepository extends JpaRepository<PasswordResetChallenge, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM PasswordResetChallenge c WHERE c.user.id = :userId")
    Optional<PasswordResetChallenge> findByUserIdForUpdate(@Param("userId") UUID userId);

    @Query("SELECT c.user.id FROM PasswordResetChallenge c WHERE c.resetTokenHash = :resetTokenHash")
    Optional<UUID> findUserIdByResetTokenHash(@Param("resetTokenHash") String resetTokenHash);
}
