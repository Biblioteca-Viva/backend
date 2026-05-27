package org.bibliotecaviva.backend.application.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.response.UserResponseDTO;
import org.bibliotecaviva.backend.application.mappers.UserMapper;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.AccountAlreadyActiveException;
import org.bibliotecaviva.backend.domain.exceptions.AccountAlreadyBlockedException;
import org.bibliotecaviva.backend.domain.exceptions.AccountNotPendingException;
import org.bibliotecaviva.backend.domain.exceptions.UserNotFoundException;
import org.bibliotecaviva.backend.persistence.repository.BookClubRepository;
import org.bibliotecaviva.backend.persistence.repository.BookClubReviewRepository;
import org.bibliotecaviva.backend.persistence.repository.CommentRepository;
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.bibliotecaviva.backend.persistence.repository.WorkRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final WorkRepository workRepository;
    private final CommentRepository commentRepository;
    private final BookClubRepository bookClubRepository;
    private final BookClubReviewRepository bookClubReviewRepository;

    /**
     * @param id Acepts any status, since blocked and rejected can change to approved(active status)
     */
    @Transactional
    public void activateUser(UUID id) {
        var user = getUser(id);
        if (user.getAccountStatus() == Status.ACTIVE) {
            throw new AccountAlreadyActiveException("User with id: " + id + " is already active");
        }
        user.setAccountStatus(Status.ACTIVE);
        userRepository.save(user);
    }

    /**
     * @param id Only accepts pending users, rejected can be changed to approved.
     */
    @Transactional
    public void rejectUser(UUID id) {
        var user = getUser(id);
        if (user.getAccountStatus() == Status.PENDING) {
            user.setAccountStatus(Status.REJECTED);
            userRepository.save(user);
        } else {
            throw new AccountNotPendingException("Only users with PENDING status can be rejected");
        }
    }

    /**
     * @param id Accepts any status, c
     */
    @Transactional
    public void blockUser(UUID id) {
        var user = getUser(id);
        if (user.getAccountStatus() == Status.BLOCKED) {
            throw new AccountAlreadyBlockedException("User with id: " + id + " is already blocked");
        }
        user.setAccountStatus(Status.BLOCKED);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        var user = getUser(id);

        commentRepository.deleteLikesFromCommentsByUserId(id);
        commentRepository.deleteAllByUserId(id);
        bookClubReviewRepository.deleteAllByUserId(id);
        userRepository.deleteCommentLikesByUserId(id);
        userRepository.deleteWorkLikesByUserId(id);
        bookClubRepository.deleteParticipantLinksByUserId(id);
        bookClubRepository.clearOrganizerByUserId(id);
        workRepository.detachAuthorByUserId(id, user.getName());

        userRepository.delete(user);
    }

    //trocar por dto
    public Page<UserResponseDTO> getAllUsers(Status status, Pageable pageable) {
        if (status != null) {
            return userRepository.findAllByAccountStatus(status, pageable)
                    .map(userMapper::toDto);
        }
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    private @NonNull User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id: " + id + " not found"));
    }

    public Long countUsers() {
        return userRepository.count();
    }

    public Long countPendingUsers() {
        return userRepository.countUserByAccountStatus(Status.PENDING);
    }

    public Optional<UserResponseDTO> findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getAccountStatus()));
    }
}
