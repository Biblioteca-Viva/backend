package org.bibliotecaviva.backend.domain.entities.textual;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bibliotecaviva.backend.domain.entities.TextualWork;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tale extends TextualWork {
    @Column(columnDefinition = "TEXT")
    private String genre;
}
