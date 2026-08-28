package org.bibliotecaviva.backend.application.services;

import org.bibliotecaviva.backend.application.dtos.request.BookClubRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.BookClubResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.SubscribeResponseDTO;
import org.bibliotecaviva.backend.application.mappers.BookClubMapper;
import org.bibliotecaviva.backend.domain.entities.BookClub;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.enums.Status;
import org.bibliotecaviva.backend.domain.exceptions.ConflictException;
import org.bibliotecaviva.backend.domain.exceptions.ForbiddenException;
import org.bibliotecaviva.backend.domain.exceptions.NotFoundException;
import org.bibliotecaviva.backend.persistence.repository.BookClubRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookClubServiceTest {

    @Mock
    private BookClubRepository bookClubRepository;

    @Mock
    private BookClubMapper bookClubMapper;

    @InjectMocks
    private BookClubService bookClubService;

    @Test
    void createShouldPersistWhenMonthHasNoBookClub() {
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        LocalDateTime date = LocalDateTime.of(2026, 8, 15, 19, 30);
        BookClubRequestDTO request = buildRequest(date);
        BookClub mapped = buildBookClub(UUID.randomUUID(), organizer, date);
        BookClubResponseDTO expected = buildResponse(mapped, 0L, BigDecimal.ZERO);

        when(bookClubMapper.toEntity(request, organizer)).thenReturn(mapped);
        when(bookClubRepository.existsBookClubByDateBetween(
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 31, 23, 59, 59))).thenReturn(false);
        when(bookClubRepository.save(mapped)).thenReturn(mapped);
        when(bookClubMapper.toDto(mapped, 0L, BigDecimal.ZERO)).thenReturn(expected);

        BookClubResponseDTO response = bookClubService.create(request, organizer);

        assertSame(expected, response);
        verify(bookClubRepository).save(mapped);
    }

    @Test
    void createShouldFailWhenMonthAlreadyHasBookClub() {
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        LocalDateTime date = LocalDateTime.of(2026, 9, 10, 19, 30);
        BookClubRequestDTO request = buildRequest(date);
        BookClub mapped = buildBookClub(UUID.randomUUID(), organizer, date);

        when(bookClubMapper.toEntity(request, organizer)).thenReturn(mapped);
        when(bookClubRepository.existsBookClubByDateBetween(
                LocalDateTime.of(2026, 9, 1, 0, 0),
                LocalDateTime.of(2026, 9, 30, 23, 59, 59))).thenReturn(true);

        assertThrows(ConflictException.class, () -> bookClubService.create(request, organizer));

        verify(bookClubRepository, never()).save(any());
    }

    @Test
    void getNextShouldMapClosestFutureBookClubWithCountsAndAverageRating() {
        UUID id = UUID.randomUUID();
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        BookClub bookClub = buildBookClub(id, organizer, LocalDateTime.now().plusDays(10));
        BigDecimal averageRating = BigDecimal.valueOf(4.5);
        BookClubResponseDTO expected = buildResponse(bookClub, 3L, averageRating);

        when(bookClubRepository.findFirstByDateAfterOrderByDateAsc(any(LocalDateTime.class))).thenReturn(Optional.of(bookClub));
        when(bookClubRepository.countParticipants(id)).thenReturn(3L);
        when(bookClubRepository.getAverageRating(id)).thenReturn(averageRating);
        when(bookClubMapper.toDto(bookClub, 3L, averageRating)).thenReturn(expected);

        BookClubResponseDTO response = bookClubService.getNext();

        assertSame(expected, response);
    }

    @Test
    void getNextShouldFailWhenThereIsNoFutureBookClub() {
        when(bookClubRepository.findFirstByDateAfterOrderByDateAsc(any(LocalDateTime.class))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookClubService.getNext());
    }

    @Test
    void getAllShouldMapParticipantCountAndRoundedAverageRating() {
        Pageable pageable = PageRequest.of(0, 10);
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        BookClub bookClub = buildBookClub(UUID.randomUUID(), organizer, LocalDateTime.now().plusDays(10));
        BigDecimal roundedRating = BigDecimal.valueOf(4.5).setScale(2, RoundingMode.HALF_UP);
        BookClubResponseDTO expected = buildResponse(bookClub, 2L, roundedRating);

        List<Object[]> rows = List.<Object[]>of(new Object[]{bookClub, 2L, 4.5d});
        when(bookClubRepository.findAllWithParticipantCountAndAverageRating(pageable))
                .thenReturn(new PageImpl<>(rows));
        when(bookClubMapper.toDto(bookClub, 2L, roundedRating)).thenReturn(expected);

        Page<BookClubResponseDTO> response = bookClubService.getAll(pageable);

        assertEquals(List.of(expected), response.getContent());
    }

    @Test
    void getByIdShouldMapExistingBookClubWithMetrics() {
        UUID id = UUID.randomUUID();
        BookClub club = buildBookClub(id, buildUser(UUID.randomUUID(), Role.CURADOR), LocalDateTime.now().plusDays(5));
        BookClubResponseDTO expected = buildResponse(club, 3L, BigDecimal.valueOf(4.2));
        when(bookClubRepository.findById(id)).thenReturn(Optional.of(club));
        when(bookClubRepository.countParticipants(id)).thenReturn(3L);
        when(bookClubRepository.getAverageRating(id)).thenReturn(BigDecimal.valueOf(4.2));
        when(bookClubMapper.toDto(club, 3L, BigDecimal.valueOf(4.2))).thenReturn(expected);

        assertSame(expected, bookClubService.getById(id));
    }

    @Test
    void getByIdShouldFailWhenBookClubDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(bookClubRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookClubService.getById(id));
    }

    @Test
    void updateShouldAllowOwnerAndRejectAnotherBookClubInSameMonth() {
        UUID id = UUID.randomUUID();
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        LocalDateTime date = LocalDateTime.of(2026, 10, 20, 19, 30);
        BookClubRequestDTO request = buildRequest(date);
        BookClub existing = buildBookClub(id, organizer, LocalDateTime.of(2026, 11, 5, 19, 30));

        when(bookClubRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bookClubRepository.existsBookClubByDateBetweenAndIdNot(
                LocalDateTime.of(2026, 10, 1, 0, 0),
                LocalDateTime.of(2026, 10, 31, 23, 59, 59),
                id)).thenReturn(true);

        assertThrows(ConflictException.class, () -> bookClubService.update(id, request, organizer));

        verify(bookClubMapper, never()).partialUpdate(any(), any());
    }

    @Test
    void updateShouldFailWhenUserIsNotOwnerOrAdmin() {
        UUID id = UUID.randomUUID();
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        User other = buildUser(UUID.randomUUID(), Role.CURADOR);
        BookClub existing = buildBookClub(id, organizer, LocalDateTime.now().plusDays(10));

        when(bookClubRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenException.class, () -> bookClubService.update(id, buildRequest(LocalDateTime.now().plusMonths(2)), other));
    }

    @Test
    void updateShouldApplyChangesWhenOwnerAndMonthIsAvailable() {
        UUID id = UUID.randomUUID();
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        LocalDateTime newDate = LocalDateTime.of(2027, 2, 12, 18, 0);
        BookClub club = buildBookClub(id, organizer, LocalDateTime.of(2027, 1, 12, 18, 0));
        BookClubRequestDTO request = buildRequest(newDate);
        BookClubResponseDTO expected = buildResponse(club, 2L, BigDecimal.valueOf(4.0));
        when(bookClubRepository.findById(id)).thenReturn(Optional.of(club));
        when(bookClubRepository.existsBookClubByDateBetweenAndIdNot(any(), any(), eq(id))).thenReturn(false);
        when(bookClubRepository.countParticipants(id)).thenReturn(2L);
        when(bookClubRepository.getAverageRating(id)).thenReturn(BigDecimal.valueOf(4.0));
        when(bookClubMapper.toDto(club, 2L, BigDecimal.valueOf(4.0))).thenReturn(expected);

        assertSame(expected, bookClubService.update(id, request, organizer));
        verify(bookClubMapper).partialUpdate(request, club);
    }

    @Test
    void subscribeShouldAddUserWhenThereIsRoom() {
        UUID id = UUID.randomUUID();
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        User student = buildUser(UUID.randomUUID(), Role.ALUNO);
        BookClub bookClub = buildBookClub(id, organizer, LocalDateTime.now().plusDays(10));

        when(bookClubRepository.findById(id)).thenReturn(Optional.of(bookClub));

        SubscribeResponseDTO response = bookClubService.subscribe(id, student);

        assertEquals("Presen\u00e7a confirmada com sucesso", response.message());
        assertTrue(bookClub.getParticipants().contains(student));
    }

    @Test
    void subscribeShouldFailWhenUserIsAlreadySubscribed() {
        UUID id = UUID.randomUUID();
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        User student = buildUser(UUID.randomUUID(), Role.ALUNO);
        BookClub bookClub = buildBookClub(id, organizer, LocalDateTime.now().plusDays(10));
        bookClub.getParticipants().add(student);

        when(bookClubRepository.findById(id)).thenReturn(Optional.of(bookClub));

        assertThrows(ConflictException.class, () -> bookClubService.subscribe(id, student));
    }

    @Test
    void subscribeShouldFailWhenParticipantLimitIsReached() {
        UUID id = UUID.randomUUID();
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        User student = buildUser(UUID.randomUUID(), Role.ALUNO);
        BookClub bookClub = buildBookClub(id, organizer, LocalDateTime.now().plusDays(10));
        for (int i = 0; i < 25; i++) {
            bookClub.getParticipants().add(buildUser(UUID.randomUUID(), Role.ALUNO));
        }

        when(bookClubRepository.findById(id)).thenReturn(Optional.of(bookClub));

        assertThrows(ConflictException.class, () -> bookClubService.subscribe(id, student));
    }

    @Test
    void unsubscribeShouldRemoveExistingParticipant() {
        UUID id = UUID.randomUUID();
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        User student = buildUser(UUID.randomUUID(), Role.ALUNO);
        BookClub bookClub = buildBookClub(id, organizer, LocalDateTime.now().plusDays(10));
        bookClub.getParticipants().add(student);

        when(bookClubRepository.findById(id)).thenReturn(Optional.of(bookClub));

        SubscribeResponseDTO response = bookClubService.unsubscribe(id, student);

        assertEquals("Presen\u00e7a cancelada com sucesso", response.message());
        assertTrue(bookClub.getParticipants().isEmpty());
    }

    @Test
    void unsubscribeShouldFailWhenUserIsNotSubscribed() {
        UUID id = UUID.randomUUID();
        User user = buildUser(UUID.randomUUID(), Role.ALUNO);
        BookClub club = buildBookClub(id, buildUser(UUID.randomUUID(), Role.CURADOR), LocalDateTime.now().plusDays(5));
        when(bookClubRepository.findById(id)).thenReturn(Optional.of(club));

        assertThrows(ConflictException.class, () -> bookClubService.unsubscribe(id, user));
    }

    @Test
    void getParticipantsShouldReturnOrganizerAndStudentNames() {
        UUID id = UUID.randomUUID();
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        User first = buildUser(UUID.randomUUID(), Role.ALUNO);
        User second = buildUser(UUID.randomUUID(), Role.ALUNO);
        first.setName("Ana");
        second.setName("Bruno");
        BookClub club = buildBookClub(id, organizer, LocalDateTime.now().plusDays(5));
        club.getParticipants().add(first);
        club.getParticipants().add(second);
        when(bookClubRepository.findById(id)).thenReturn(Optional.of(club));

        var response = bookClubService.getParticipants(id);

        assertEquals(organizer.getName(), response.organizer());
        assertEquals(2, response.students().size());
        assertTrue(response.students().containsAll(List.of("Ana", "Bruno")));
    }

    @Test
    void getParticipantsShouldFailWhenBookClubDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(bookClubRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookClubService.getParticipants(id));
    }

    @Test
    void deleteShouldAllowAdminEvenWhenNotOrganizer() {
        UUID id = UUID.randomUUID();
        User organizer = buildUser(UUID.randomUUID(), Role.CURADOR);
        User admin = buildUser(UUID.randomUUID(), Role.ADMIN);
        BookClub bookClub = buildBookClub(id, organizer, LocalDateTime.now().plusDays(10));

        when(bookClubRepository.findById(id)).thenReturn(Optional.of(bookClub));

        bookClubService.delete(id, admin);

        ArgumentCaptor<BookClub> captor = ArgumentCaptor.forClass(BookClub.class);
        verify(bookClubRepository).delete(captor.capture());
        assertSame(bookClub, captor.getValue());
    }

    private static BookClubRequestDTO buildRequest(LocalDateTime date) {
        return new BookClubRequestDTO(
                "Dom Casmurro",
                "Sinopse valida para teste",
                "Machado de Assis",
                date,
                "Biblioteca Municipal",
                "https://example.com/capa.jpg"
        );
    }

    private static BookClub buildBookClub(UUID id, User organizer, LocalDateTime date) {
        return BookClub.builder()
                .id(id)
                .bookName("Dom Casmurro")
                .bookSynopses("Sinopse valida para teste")
                .bookAuthor("Machado de Assis")
                .date(date)
                .location("Biblioteca Municipal")
                .bookCoverUrl("https://example.com/capa.jpg")
                .organizer(organizer)
                .participants(new HashSet<>())
                .build();
    }

    private static BookClubResponseDTO buildResponse(BookClub bookClub, Long participantsCount, BigDecimal averageRating) {
        return new BookClubResponseDTO(
                bookClub.getId(),
                bookClub.getOrganizer().getName(),
                bookClub.getBookName(),
                bookClub.getBookSynopses(),
                bookClub.getBookAuthor(),
                bookClub.getDate(),
                bookClub.getLocation(),
                participantsCount,
                bookClub.getBookCoverUrl(),
                averageRating
        );
    }

    private static User buildUser(UUID id, Role role) {
        return User.builder()
                .id(id)
                .name(role == Role.ADMIN ? "Admin" : "Usuario")
                .email(id + "@teste.com")
                .password("123456")
                .role(role)
                .accountStatus(Status.ACTIVE)
                .build();
    }
}
