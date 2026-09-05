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
    @Column(columnDefinition = "TEXT")
    private String bookSynopses;
    private String bookAuthor;
    private LocalDateTime date;
    @Column(columnDefinition = "TEXT")
    private String bookCoverUrl;
    private String location; // Deixar por enquanto mas ta hardcordado na biblioteca
    @ManyToOne //todo: por lazy em todso, authorize nos controlers
    @JoinColumn(name = "organizer_id")
    private User organizer;
    @ManyToMany
    @JoinTable(
            name = "book_club_participants",
            joinColumns = @JoinColumn(name = "book_club_id"),
            inverseJoinColumns = @JoinColumn(name = "users_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "bookClub", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BookClubReview> reviews;
    //pode associar resenhas relacionadas caso sejam obras autorais como as outras. ai filtra depois as resenhas
    //pelo nome do livro
}
