package org.bibliotecaviva.backend.persistance.repository;

import org.bibliotecaviva.backend.domain.entities.Work;
import org.bibliotecaviva.backend.domain.entities.WorkSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkRepository extends  JpaRepository<Work, UUID> {
    @Query(value = """
    SELECT *
    FROM obras
    WHERE (:type IS NULL OR type = :type)
    """, nativeQuery = true)
    List<WorkSummary> findAllSummary(@Param("type") String type);
}
