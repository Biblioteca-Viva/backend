package org.bibliotecaviva.backend.persistance.repository;

import org.bibliotecaviva.backend.domain.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByWorkIdOrderByCreatedAtDesc(UUID workId, Pageable pageable);

    Long countByWork_Id(UUID workId);
}
