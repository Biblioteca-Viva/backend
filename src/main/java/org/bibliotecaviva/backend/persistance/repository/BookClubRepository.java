package org.bibliotecaviva.backend.persistance.repository;

import org.bibliotecaviva.backend.domain.entities.BookClub;
import org.bibliotecaviva.backend.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookClubRepository extends JpaRepository<BookClub, UUID> {
    Optional<BookClub> findFirstByDateAfterOrderByDateAsc(LocalDateTime dateAfter);

    @Query(value = "SELECT COUNT(*) FROM book_club_participants WHERE book_club_id = :id", nativeQuery = true)
    Long countParticipants(UUID id);

    @Query("SELECT b, COUNT(p) FROM BookClub b LEFT JOIN b.participants p GROUP BY b")
    List<Object[]> findAllWithParticipantCount();

    @Query("SELECT b, COUNT(p) FROM BookClub b LEFT JOIN b.participants p WHERE b.organizer = :organizer GROUP BY b")
    List<Object[]> findAllWithParticipantCountByOrganizer(@Param("organizer") User organizer);

}