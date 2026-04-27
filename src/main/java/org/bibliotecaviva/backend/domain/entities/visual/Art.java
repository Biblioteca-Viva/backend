package org.bibliotecaviva.backend.domain.entities.visual;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bibliotecaviva.backend.domain.entities.textual.Cordel;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("Art")
@SuperBuilder
/*
aqui fala que Espaço digital para divulgação de desenhos, ilustrações feitas para contos dos colegas,
 capas criadas para histórias, quadrinhos curtos, arte digital e outras expressões artísticas dos
 alunos.
talvez quebrar depois em entidades menores.
 */
public class Art extends VisualWork {
    @OneToOne(mappedBy = "illustration")
    Cordel cordel;
}
