package org.bibliotecaviva.backend.domain.entities.audiovisual;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bibliotecaviva.backend.domain.entities.AudioVisualWork;

@Entity
@Getter
@Setter
@NoArgsConstructor
/*
aqui tambem tem diversas subcategorias  produções audiovisuais feitas por alunos
(curtas-metragens, vídeos-poemas, entrevistas)
 */
public class Multimedia extends AudioVisualWork {

}
