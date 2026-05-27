package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.application.dtos.response.UserResponseDTO;
import org.bibliotecaviva.backend.application.mappers.UserMapper;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private WorkRepository workRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookClubRepository bookClubRepository;

    @Mock
    private BookClubReviewRepository bookClubReviewRepository;

    @InjectMocks
    private UserManagementService userManagementService;

    @Test
    void activateUserShouldSetStatusActiveAndSave() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, Status.PENDING);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userManagementService.activateUser(id);

        assertEquals(Status.ACTIVE, user.getAccountStatus());
        verify(userRepository).save(user);
    }

    @Test
    void activateUserShouldFailWhenAlreadyActive() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, Status.ACTIVE);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThrows(AccountAlreadyActiveException.class, () -> userManagementService.activateUser(id));

        verify(userRepository, never()).save(user);
    }

    @Test
    void activateUserShouldFailWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userManagementService.activateUser(id));

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectUserShouldSetStatusRejectedWhenPending() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, Status.PENDING);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userManagementService.rejectUser(id);

        assertEquals(Status.REJECTED, user.getAccountStatus());
        verify(userRepository).save(user);
    }

    @Test
    void rejectUserShouldFailWhenUserIsNotPending() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, Status.ACTIVE);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThrows(AccountNotPendingException.class, () -> userManagementService.rejectUser(id));

        verify(userRepository, never()).save(user);
    }

    @Test
    void blockUserShouldSetStatusBlockedAndSave() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, Status.ACTIVE);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userManagementService.blockUser(id);

        assertEquals(Status.BLOCKED, user.getAccountStatus());
        verify(userRepository).save(user);
    }

    @Test
    void blockUserShouldFailWhenAlreadyBlocked() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, Status.BLOCKED);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThrows(AccountAlreadyBlockedException.class, () -> userManagementService.blockUser(id));

        verify(userRepository, never()).save(user);
    }

    @Test
    void deleteUserShouldClearRelationshipsAndDeleteUser() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, Status.ACTIVE);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userManagementService.deleteUser(id);

        verify(commentRepository).deleteLikesFromCommentsByUserId(id);
        verify(commentRepository).deleteAllByUserId(id);
        verify(bookClubReviewRepository).deleteAllByUserId(id);
        verify(userRepository).deleteCommentLikesByUserId(id);
        verify(userRepository).deleteWorkLikesByUserId(id);
        verify(bookClubRepository).deleteParticipantLinksByUserId(id);
        verify(bookClubRepository).clearOrganizerByUserId(id);
        verify(workRepository).detachAuthorByUserId(id, user.getName());
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserShouldFailWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userManagementService.deleteUser(id));

        verify(commentRepository, never()).deleteAllByUserId(org.mockito.ArgumentMatchers.any());
        verify(userRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getAllUsersShouldUseStatusFilterWhenProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = buildUser(UUID.randomUUID(), Status.PENDING);
        UserResponseDTO dto = new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getAccountStatus());
        when(userRepository.findAllByAccountStatus(Status.PENDING, pageable)).thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toDto(user)).thenReturn(dto);

        Page<UserResponseDTO> response = userManagementService.getAllUsers(Status.PENDING, pageable);

        assertEquals(List.of(dto), response.getContent());
        verify(userRepository).findAllByAccountStatus(Status.PENDING, pageable);
        verify(userRepository, never()).findAll(pageable);
    }

    @Test
    void getAllUsersShouldReturnAllUsersWhenStatusIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = buildUser(UUID.randomUUID(), Status.ACTIVE);
        UserResponseDTO dto = new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getAccountStatus());
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toDto(user)).thenReturn(dto);

        Page<UserResponseDTO> response = userManagementService.getAllUsers(null, pageable);

        assertEquals(List.of(dto), response.getContent());
        verify(userRepository).findAll(pageable);
        verify(userRepository, never()).findAllByAccountStatus(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(pageable));
    }

    @Test
    void countUsersShouldDelegateToRepository() {
        when(userRepository.count()).thenReturn(12L);

        assertEquals(12L, userManagementService.countUsers());
    }

    @Test
    void countPendingUsersShouldDelegateToRepositoryWithPendingStatus() {
        when(userRepository.countUserByAccountStatus(Status.PENDING)).thenReturn(3L);

        assertEquals(3L, userManagementService.countPendingUsers());
        verify(userRepository).countUserByAccountStatus(Status.PENDING);
    }

    @Test
    void findUserByEmailShouldReturnUserResponseWhenFound() {
        User user = buildUser(UUID.randomUUID(), Status.ACTIVE);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Optional<UserResponseDTO> response = userManagementService.findUserByEmail(user.getEmail());

        assertTrue(response.isPresent());
        assertEquals(user.getId(), response.get().id());
        assertEquals(user.getName(), response.get().name());
        assertEquals(user.getEmail(), response.get().email());
        assertEquals(user.getRole(), response.get().role());
        assertEquals(user.getAccountStatus(), response.get().accountStatus());
    }

    @Test
    void findUserByEmailShouldReturnEmptyWhenUserDoesNotExist() {
        String email = "ausente@teste.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<UserResponseDTO> response = userManagementService.findUserByEmail(email);

        assertTrue(response.isEmpty());
        verify(userRepository).findByEmail(email);
    }

    private static User buildUser(UUID id, Status status) {
        return User.builder()
                .id(id)
                .name("Usuario")
                .email("usuario@teste.com")
                .password("123456")
                .role(Role.ALUNO)
                .accountStatus(status)
                .build();
    }
}
