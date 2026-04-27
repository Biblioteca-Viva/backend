package org.bibliotecaviva.backend.domain.entities.audiovisual;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bibliotecaviva.backend.domain.entities.Work;
import org.bibliotecaviva.backend.persistence.converter.DurationConverter;

import java.time.Duration;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AudioVisualWork extends Work {
    @Column(columnDefinition = "TEXT")
    private String url;
    @Convert(converter = DurationConverter.class)
    private Duration duration;
}
