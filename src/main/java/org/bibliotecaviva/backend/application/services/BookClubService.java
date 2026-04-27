package org.bibliotecaviva.backend.application.services;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.BookClubRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.BookClubResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.SubscribeResponseDTO;
import org.bibliotecaviva.backend.application.mappers.BookClubMapper;
import org.bibliotecaviva.backend.domain.entities.BookClub;
import org.bibliotecaviva.backend.domain.entities.User;
import org.bibliotecaviva.backend.domain.enums.Role;
import org.bibliotecaviva.backend.domain.exceptions.ConflictException;
import org.bibliotecaviva.backend.domain.exceptions.ForbiddenException;
import org.bibliotecaviva.backend.domain.exceptions.NotFoundException;
import org.bibliotecaviva.backend.persistence.repository.BookClubRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookClubService {

    private final BookClubRepository bookClubRepository;
    private final BookClubMapper bookClubMapper;

    @Transactional
    public BookClubResponseDTO create(BookClubRequestDTO requestDTO, User user) {
        var bookClub = bookClubMapper.toEntity(requestDTO,user);
        return bookClubMapper.toDto(bookClubRepository.save(bookClub), 0L);
    }

    public BookClubResponseDTO getNext() {
        var next = bookClubRepository.findFirstByDateAfterOrderByDateAsc(LocalDateTime.now())
                .orElseThrow(() -> new NotFoundException("Nenhum clube do livro pra exibir"));
        return bookClubMapper.toDto(next, bookClubRepository.countParticipants(next.getId()));
    }

    public Page<BookClubResponseDTO> getAll(Pageable pageable) {
        return bookClubRepository.findAllWithParticipantCount(pageable)
                .map(club -> bookClubMapper.toDto((BookClub) club[0], (Long) club[1])
                );

    }

    public BookClubResponseDTO getById(UUID id) {
        var work = bookClubRepository.findById(id).orElseThrow(() -> new NotFoundException("CLUBE de leitura nao encontrado"));
        return bookClubMapper.toDto(work, bookClubRepository.countParticipants(id));
    }

    @Transactional
    public void delete(UUID id, User user) {
        var club = bookClubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube do livro com id " + id + " não encontrado"));
        verifyOwnership(user, club);
        bookClubRepository.delete(club);
    }

    @Transactional
    public BookClubResponseDTO update(UUID id, @Valid BookClubRequestDTO requestDTO, User user) {
        var bookClub = bookClubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube do livro com id " + id + " não encontrado"));
        verifyOwnership(user, bookClub);
        bookClubMapper.partialUpdate(requestDTO, bookClub);
        return bookClubMapper.toDto(bookClub, bookClubRepository.countParticipants(id));
    }

    @Transactional
    public SubscribeResponseDTO subscribe(UUID id, User user) {
        //todo: adicionar capacidade maxima se precisar,pode variar de acordocom o local, caso seja somente presencial da
        //      pra fzer uns enums e mapear corretamete, por enquanto ta capado em 100 hardcoded
        var bookClub = bookClubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube do livro com id " + id + " não encontrado"));
        if (bookClub.getParticipants().contains(user)) {
            throw new ConflictException("Usuário já inscrito");
        }
        if (bookClub.getParticipants().size() >= 100) {
            throw new ConflictException("Limite de participantes atingido");
        }
        bookClub.getParticipants().add(user);
        return new SubscribeResponseDTO("Presença confirmada com sucesso");
    }

    @Transactional
    public SubscribeResponseDTO unsubscribe(UUID id, User user) {
        var bookClub = bookClubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube do livro com id " + id + " não encontrado"));
        if (!bookClub.getParticipants().contains(user)) {
            throw new ConflictException("Usuário não está inscrito");
        }
        bookClub.getParticipants().remove(user);
        return new SubscribeResponseDTO("Presença cancelada com sucesso");
    }

    private static void verifyOwnership(User user, BookClub club) {
        if (!club.getOrganizer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Forbidden");
        }
    }

}
