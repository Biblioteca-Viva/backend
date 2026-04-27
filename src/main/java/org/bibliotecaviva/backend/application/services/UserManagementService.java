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
import org.bibliotecaviva.backend.persistence.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
}
