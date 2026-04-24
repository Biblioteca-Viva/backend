package org.bibliotecaviva.backend.persistance.repository;

import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    //todo: se like virar classe criar um like repository
    @Query(value = "SELECT COUNT(*) > 0 FROM likes WHERE user_id = :userId AND work_id = :workId", nativeQuery = true)
    boolean existsLike(@Param("userId") UUID userId, @Param("workId") UUID workId);

    @Query(value = "SELECT work_id FROM likes WHERE user_id = :userId", nativeQuery = true)
    List<UUID> findLikedWorkIdsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query(value = "INSERT INTO likes (user_id, work_id) VALUES (:userId, :workId) ON CONFLICT (user_id, work_id) DO NOTHING", nativeQuery = true)
    void likeWork(@Param("userId") UUID userId, @Param("workId") UUID workId);

    @Modifying
    @Query(value = "DELETE FROM likes WHERE user_id = :userId AND work_id = :workId", nativeQuery = true)
    void unlikeWork(@Param("userId") UUID userId, @Param("workId") UUID workId);

    Page<User> findAllByAccountStatus(Status accountStatus, Pageable pageable);

    boolean existsByEmail(String email);

    Long countUserByAccountStatus(Status accountStatus);
}
