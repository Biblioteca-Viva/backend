package org.bibliotecaviva.backend.domain.entities.textual;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder

@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("Cordel")
public class Cordel extends TextualWork {

    private String rhymeScheme;
    //ilistracao como ta no banco colocar dps
}
