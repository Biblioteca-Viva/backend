package org.bibliotecaviva.backend.application.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.response.CommentReplyResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.CommentResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.CommentSummaryResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.LikeResponseDTO;
import org.bibliotecaviva.backend.domain.entities.Comment;
import org.bibliotecaviva.backend.domain.entities.CommentReply;
import org.bibliotecaviva.backend.domain.entities.projections.CommentSummary;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.exceptions.CommentNotFoundException;
import org.bibliotecaviva.backend.domain.exceptions.ReplyAlreadyExistsException;
import org.bibliotecaviva.backend.domain.exceptions.WorkNotFoundException;
import org.bibliotecaviva.backend.persistence.repository.CommentReplyRepository;
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
    private final CommentReplyRepository replyRepository;
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

    public Page<CommentSummaryResponseDTO> getAll(Pageable pageable){ // todo: ta extourando n+1 dps corrijo
        return commentRepository.findAllWithDetails(pageable)
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
        commentRepository.delete(comment);
    }

    //usar filtrando já por work
    private CommentResponseDTO toDTO(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getName(),
                comment.getCreatedAt(),
                commentRepository.getLikeCount(comment.getId()),
                comment.getReply() != null ? toReplyDTO(comment.getReply()) : null
        );
    }
    //somente pra dashboard
    private CommentSummaryResponseDTO toSummaryDTO(CommentSummary comment) {
        return new CommentSummaryResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUserName(),
                comment.getUserId(),
                comment.getWorkTitle(),
                comment.getWorkId(),
                comment.getCreatedAt(),
                comment.getReplyId(),
                comment.getReplyContent(),
                comment.getReplyAuthor(),
                comment.getReplyCreatedAt()
        );
    }

    public Long countComments(){
        return commentRepository.count();
    }

    @Transactional
    public LikeResponseDTO like(UUID id, User user) {
        commentRepository.likeComment(user.getId(), id);
        long likeCount = commentRepository.getLikeCount(id);
        return new LikeResponseDTO(true, likeCount);
    }

    @Transactional
    public LikeResponseDTO unLike(UUID id, User user) {
        commentRepository.unlikeComment(user.getId(), id);
        long likeCount = commentRepository.getLikeCount(id);
        return new LikeResponseDTO(false, likeCount);
    }

    @Transactional
    public CommentReplyResponseDTO reply(UUID commentId, String content, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comentário com id " + commentId + " não encontrado"));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isWorkOwner = comment.getWork().getAuthor().getId().equals(user.getId());

        if (!isAdmin && !isWorkOwner) {
            throw new AccessDeniedException("Apenas administradores ou o autor da obra podem responder comentários");
        }
        if(comment.getReply() != null){
            throw new ReplyAlreadyExistsException("Só pode existir uma resposta para esse comentário!");
        }
        CommentReply reply = CommentReply.builder()
                .content(content)
                .comment(comment)
                .user(user)
                .build();

        comment.setReply(reply);

        return toReplyDTO(replyRepository.save(reply));
    }

    public CommentReplyResponseDTO getByReplyByCommentId(UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException("Comentário com id " + commentId + " não encontrado");
        }
        var reply = replyRepository.findByCommentId(commentId)
                .orElseThrow(()-> new CommentNotFoundException("Não existe resposta para esse comentario"));
        return toReplyDTO(reply);
    }

    @Transactional
    public void deleteReply(UUID replyId, User user) {
        CommentReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new CommentNotFoundException("Resposta com id " + replyId + " não encontrada"));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = reply.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Você não pode deletar esta resposta");
        }
        replyRepository.delete(reply);
    }

    private CommentReplyResponseDTO toReplyDTO(CommentReply reply) {
        return new CommentReplyResponseDTO(
                reply.getId(),
                reply.getContent(),
                reply.getUser().getName(),
                reply.getCreatedAt()
        );
    }

    @Transactional
    public CommentReplyResponseDTO updateReply(UUID commentId, String content, User user) {
        CommentReply reply = replyRepository.findByCommentId(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Resposta não encontrada"));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = reply.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Você não pode editar esta resposta");
        }

        reply.setContent(content);
        return toReplyDTO(replyRepository.save(reply));
    }
}

