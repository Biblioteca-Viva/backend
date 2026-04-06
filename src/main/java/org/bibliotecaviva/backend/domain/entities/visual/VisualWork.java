package org.bibliotecaviva.backend.domain.entities.visual;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bibliotecaviva.backend.domain.entities.Work;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public abstract class  VisualWork extends Work {
    private String url;
}
