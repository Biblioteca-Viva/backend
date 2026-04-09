package org.bibliotecaviva.backend.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.Arrays;

@Getter
public enum WorkTypes {
    ARTICLE("Article"),
    LIBRA_LITERATURE("LibraLiterature"),
    SHORT_STORY("ShortStory"),
    TALE("Tale"),
    ART("Art"),
    INFOGRAPHIC("Infographic"),
    MULTIMEDIA("Multimedia"),
    CORDEL("Cordel"),
    ESSAY("Essay");

    @JsonValue
    private final String value;

    WorkTypes(String value) {
        this.value = value;
    }
    @JsonCreator
    public static WorkTypes fromString(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return Arrays.stream(WorkTypes.values())
                .filter(type -> type.value.equalsIgnoreCase(text) || type.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de obra inválido: " + text));
    }
}