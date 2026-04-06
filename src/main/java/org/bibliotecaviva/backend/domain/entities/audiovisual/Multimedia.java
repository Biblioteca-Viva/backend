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
@DiscriminatorValue("Multimedia")
@SuperBuilder

/*
aqui tambem tem diversas subcategorias  produções audiovisuais feitas por alunos
(curtas-metragens, vídeos-poemas, entrevistas)
 */
public class Multimedia extends AudioVisualWork {

}
