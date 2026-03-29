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
public class Cordel extends TextualWork {

    private String rhymeScheme;
    //ilistracao como ta no banco colocar dps
}
