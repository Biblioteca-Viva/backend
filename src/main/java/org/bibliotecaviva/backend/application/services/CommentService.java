package org.bibliotecaviva.backend.application.services;

import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.response.CommentResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.CommentSummaryResponseDTO;
import org.bibliotecaviva.backend.domain.entities.Comment;
import org.bibliotecaviva.backend.domain.entities.CommentSummary;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.exceptions.CommentNotFoundException;
import org.bibliotecaviva.backend.domain.exceptions.WorkNotFoundException;
import org.bibliotecaviva.backend.persistence.repository.CommentRepository;
import org.bibliotecaviva.backend.persistence.repository.WorkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.bibliotecaviva.backend.domain.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final WorkRepository workRepository;

    //todo: Verificar spam, criar algo que não permita

    @Transactional
    public CommentResponseDTO create(UUID workId, String content, User user) {
        var work = workRepository.findById(workId)
                .orElseThrow(() -> new WorkNotFoundException("Obra com id " + workId + " não encontrada"));

        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .work(work)
                .build();

        Comment saved = commentRepository.save(comment);
        return toDTO(saved);
    }

    public Page<CommentResponseDTO> getByWorkId(UUID workId, Pageable pageable) {
        if (!workRepository.existsById(workId)) {
            throw new WorkNotFoundException("Obra com id " + workId + " não encontrada");
        }
        return commentRepository.findByWorkIdOrderByCreatedAtDesc(workId, pageable)
                .map(this::toDTO);
    }

    public Page<CommentSummaryResponseDTO> getAll(Pageable pageable){
        return commentRepository.findAllWithUserAndWork(pageable)
                .map(this::toSummaryDTO);
    }
    @Transactional
    public CommentResponseDTO update(UUID commentId, User user, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comentário com id " + commentId + " não encontrado"));

        boolean isOwner = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if(!isOwner && !isAdmin) {
            throw new AccessDeniedException("Você não pode editar este comentário");
        }
        comment.setContent(content);
        return toDTO(commentRepository.save(comment));
    }

    
    @Transactional
    public void delete(UUID commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comentário com id " + commentId + " não encontrado"));
        
        boolean isOwner = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        
        if(!isOwner && !isAdmin) {
            throw new AccessDeniedException("Você não pode deletar este comentário");
        }
        commentRepository.deleteById(commentId);
    }

    //usar filtrando já por work
    private CommentResponseDTO toDTO(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getName(),
                comment.getCreatedAt()
        );
    }
    //somente pra dashboard
    private CommentSummaryResponseDTO toSummaryDTO(CommentSummary comment) {
        return new CommentSummaryResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUserName(),
                comment.getWorkTitle(),
                comment.getCreatedAt()
        );
    }

    public Long countComments(){
        return commentRepository.count();
    }
}
