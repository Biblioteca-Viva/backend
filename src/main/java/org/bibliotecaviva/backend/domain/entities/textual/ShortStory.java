package org.bibliotecaviva.backend.domain.entities.textual;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bibliotecaviva.backend.domain.entities.TextualWork;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ShortStory extends TextualWork {
}
