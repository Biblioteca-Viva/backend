package org.bibliotecaviva.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookClub {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String bookName;
    private String bookSynopses;
    private String bookAuthor;
    // private String bookCoverUrl;
    private LocalDateTime date;
    private String location;
    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;
    @ManyToMany
    @JoinTable(
            name = "book_club_participants",
            joinColumns = @JoinColumn(name = "book_club_id"),
            inverseJoinColumns = @JoinColumn(name = "users_id")
    )
    private Set<User> participants = new HashSet<>();
    //pode associar resenhas relacionadas caso sejam obras autorais como as outras. ai filtra depois as resenhas
    //pelo nome do livro
}
