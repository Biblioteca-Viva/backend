package org.bibliotecaviva.backend.persistance.repository;

import jakarta.transaction.Transactional;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.entities.Work;
import org.bibliotecaviva.backend.domain.entities.WorkSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkRepository extends  JpaRepository<Work, UUID> {

    @Query(value = """
    SELECT w.id, w.title, w.publication_date, w.description, w.type, w.view_count,
           u.name as author,
           COUNT(l.user_id) as like_count
    FROM obras w
    JOIN users u ON u.id = w.users_id
    LEFT JOIN likes l ON l.work_id = w.id
    WHERE (:type IS NULL OR w.type = :type)
    GROUP BY w.id, w.title, w.publication_date, w.description, w.type, w.view_count, u.name
    """, nativeQuery = true)
    List<WorkSummary> findAllSummary(@Param("type") String type);

    @Modifying
    @Transactional
    @Query("UPDATE Work w SET w.viewCount = w.viewCount + 1 WHERE w.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Query(value = "SELECT COUNT(*) FROM likes WHERE work_id = :workId", nativeQuery = true)
    long getLikeCount(@Param("workId") UUID workId);

    boolean existsWorkByAuthorAndTitle(User author, String title);
}
