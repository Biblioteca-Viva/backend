package org.bibliotecaviva.backend.persistence.repository;

import org.bibliotecaviva.backend.application.mappers.BookClubMapper;
import org.bibliotecaviva.backend.domain.entities.CommentReply;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentReplyRepository extends JpaRepository<CommentReply, UUID> {

    Optional<CommentReply> findByCommentId(UUID commentId);
}
