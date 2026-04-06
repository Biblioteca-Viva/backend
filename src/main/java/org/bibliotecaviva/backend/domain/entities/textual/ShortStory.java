package org.bibliotecaviva.backend.domain.entities.textual;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("ShortStory")
@SuperBuilder

public class ShortStory extends TextualWork {
}
