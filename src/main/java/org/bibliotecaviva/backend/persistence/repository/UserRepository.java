package org.bibliotecaviva.backend.persistence.repository;

import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.accountStatus = :status")
    Optional<User> findByEmailAndStatusForUpdate(@Param("email") String email, @Param("status") Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    //todo: se like virar classe criar um like repository
    @Query(value = "SELECT work_id FROM likes WHERE user_id = :userId", nativeQuery = true)
    List<UUID> findLikedWorkIdsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query(value = """
            INSERT INTO likes (user_id, work_id)
            SELECT :userId, :workId
            WHERE NOT EXISTS (
                SELECT 1 FROM likes WHERE user_id = :userId AND work_id = :workId
            )
            """, nativeQuery = true)
    void likeWork(@Param("userId") UUID userId, @Param("workId") UUID workId);

    @Modifying
    @Query(value = "DELETE FROM likes WHERE user_id = :userId AND work_id = :workId", nativeQuery = true)
    void unlikeWork(@Param("userId") UUID userId, @Param("workId") UUID workId);

    @Modifying
    @Query(value = "DELETE FROM likes WHERE user_id = :userId", nativeQuery = true)
    void deleteWorkLikesByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query(value = "DELETE FROM comment_likes WHERE user_id = :userId", nativeQuery = true)
    void deleteCommentLikesByUserId(@Param("userId") UUID userId);

    Page<User> findAllByAccountStatus(Status accountStatus, Pageable pageable);

    boolean existsByEmail(String email);

    Long countUserByAccountStatus(Status accountStatus);
}
