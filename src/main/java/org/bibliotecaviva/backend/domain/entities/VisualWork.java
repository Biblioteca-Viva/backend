package org.bibliotecaviva.backend.domain.entities;

import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bibliotecaviva.backend.persistance.converter.DurationConverter;

import java.time.Duration;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public abstract class  VisualWork extends Work {
    private String url;
    @Convert(converter = DurationConverter.class)
    private Duration duration;

}
