package org.bibliotecaviva.backend.persistence.repository;

import jakarta.transaction.Transactional;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.entities.Work;
import org.bibliotecaviva.backend.domain.entities.projections.WorkSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkRepository extends JpaRepository<Work, UUID> {

    @Query(value = """
            SELECT w.id, w.title, w.publication_date, w.description, w.type, w.view_count,
                   u.name as author,
                   COALESCE(lk.like_count, 0)    as like_count,
                   COALESCE(cm.comment_count, 0) as comment_count,
                    COALESCE(a.url, i.url, lt.url, mt.url,cordel_illustration.url) as url,
                    COALESCE(mt.duration,lt.duration) as duration
            FROM obras w
            JOIN users u ON u.id = w.users_id
            LEFT JOIN public.art a on w.id = a.id
            LEFT JOIN public.infographic i on w.id = i.id
            LEFT JOIN public.libra_literature lt on w.id =lt.id
            LEFT JOIN multimedia mt on w.id = mt.id
            LEFT JOIN public.cordel c on w.id = c.id
                LEFT JOIN public.art cordel_illustration on cordel_illustration.id = c.illustration_id
            LEFT JOIN (
                SELECT work_id, COUNT(user_id) as like_count
                FROM likes
                GROUP BY work_id
            ) lk ON lk.work_id = w.id
            LEFT JOIN (
                SELECT work_id, COUNT(id) as comment_count
                FROM comments
                GROUP BY work_id
            ) cm ON cm.work_id = w.id
            WHERE (:type IS NULL OR w.type = :type)
            """,
            countQuery = """
            SELECT COUNT(*) FROM obras w
            WHERE (:type IS NULL OR w.type = :type)
            """,
            nativeQuery = true)
    Page<WorkSummary> findAllSummary(@Param("type") String type, Pageable pageable);

    @Query(value = "SELECT w.type, COUNT(w) FROM obras w GROUP BY w.type",nativeQuery = true)
    List<Object[]> countPerType();

    @Query(value = """
    SELECT w.id, w.title, w.publication_date, w.description, w.type, w.view_count,
           u.name as author,
           COALESCE(lk.like_count, 0) as like_count,
           COALESCE(cm.comment_count, 0) as comment_count
    FROM obras w
    JOIN users u ON u.id = w.users_id
    LEFT JOIN (SELECT work_id, COUNT(user_id) as like_count FROM likes GROUP BY work_id) lk ON lk.work_id = w.id
    LEFT JOIN (SELECT work_id, COUNT(id) as comment_count FROM comments GROUP BY work_id) cm ON cm.work_id = w.id
    WHERE w.type = :type
    ORDER BY w.publication_date DESC
    LIMIT 5
    """, nativeQuery = true)
    List<WorkSummary> findTop5ByType(@Param("type") String type);

    @Modifying
    @Transactional
    @Query("UPDATE Work w SET w.viewCount = w.viewCount + 1 WHERE w.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Query(value = "SELECT COUNT(*) FROM likes WHERE work_id = :workId", nativeQuery = true)
    long getLikeCount(@Param("workId") UUID workId);

    boolean existsWorkByAuthorAndTitle(User author, String title);

    @Query(value = """
        SELECT w.id, w.title, w.publication_date, w.description, w.type, w.view_count,
                   u.name as author,
                   COALESCE(lk.like_count, 0)    as like_count,
                   COALESCE(cm.comment_count, 0) as comment_count,
                    COALESCE(a.url, i.url, lt.url, mt.url) as url,
                    COALESCE(mt.duration,lt.duration) as duration
            FROM obras w
            JOIN users u ON u.id = w.users_id
            LEFT JOIN public.art a on w.id = a.id
            LEFT JOIN public.infographic i on w.id = i.id
            LEFT JOIN public.libra_literature lt on w.id =lt.id
            LEFT JOIN multimedia mt on w.id = mt.id
            LEFT JOIN (
                SELECT work_id, COUNT(user_id) as like_count
                FROM likes
                GROUP BY work_id
            ) lk ON lk.work_id = w.id
            LEFT JOIN (
                SELECT work_id, COUNT(id) as comment_count
                FROM comments
                GROUP BY work_id
            ) cm ON cm.work_id = w.id
            ORDER BY like_count DESC
            LIMIT 5
""",nativeQuery = true)
    List<WorkSummary> getMostLikedWorks();


    Optional<Work> findWorkByTitle(String title);
}
