package org.bibliotecaviva.backend.domain.entities.visual;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bibliotecaviva.backend.domain.entities.VisualWork;

@Entity
@Getter
@Setter
@NoArgsConstructor
/*
aqui fala que Espaço digital para divulgação de desenhos, ilustrações feitas para contos dos colegas,
 capas criadas para histórias, quadrinhos curtos, arte digital e outras expressões artísticas dos
 alunos.
talvez quebrar depois em entidades menores.
 */
public class Art extends VisualWork {
}
