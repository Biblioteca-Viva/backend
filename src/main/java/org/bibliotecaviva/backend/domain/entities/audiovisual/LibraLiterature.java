package org.bibliotecaviva.backend.domain.entities.audiovisual;

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
@DiscriminatorValue("LibraLiterature")
@SuperBuilder

public class LibraLiterature extends AudioVisualWork {
}
