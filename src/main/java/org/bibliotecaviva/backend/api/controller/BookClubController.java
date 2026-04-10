package org.bibliotecaviva.backend.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bibliotecaviva.backend.application.dtos.request.BookClubRequestDTO;
import org.bibliotecaviva.backend.application.dtos.response.BookClubResponseDTO;
import org.bibliotecaviva.backend.application.dtos.response.SubscribeResponseDTO;
import org.bibliotecaviva.backend.application.services.BookClubService;
import org.bibliotecaviva.backend.domain.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookclub")
@RequiredArgsConstructor
public class BookClubController {
    private final BookClubService bookClubService;

    @PostMapping //todo: falta permitir prof e verificar se é o dono para permitir edição e remoção
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<BookClubResponseDTO> create(@RequestBody @Valid BookClubRequestDTO requestDTO,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookClubService.create(requestDTO,user));
    }

    @GetMapping("/next")
    public ResponseEntity<BookClubResponseDTO> getNext() {
        return ResponseEntity.ok(bookClubService.getNext());
    }

    //todo: Revisar getAll e getById, colocar paginação, se for somente 1 por mes, deixar restrito para professor e admin
    //      se for varios, vai ter que ter um getByMonth ou algo do tipo, ou deixar como ta e sempre aparecer os proximos
    @GetMapping
    public ResponseEntity<List<BookClubResponseDTO>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookClubService.getAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<BookClubResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookClubService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<BookClubResponseDTO> update(@PathVariable UUID id,
                                                      @RequestBody @Valid BookClubRequestDTO requestDTO,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookClubService.update(id, requestDTO, user));
    }

    @DeleteMapping("/{id}")//so admin/prof
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal User user) {
        bookClubService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/subscribe")
    public ResponseEntity<SubscribeResponseDTO> subscribe(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookClubService.subscribe(id, user));
    }

    @PostMapping("{id}/unsubscribe")
    public ResponseEntity<SubscribeResponseDTO> unsubscribe(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookClubService.unsubscribe(id, user));
    }

}
