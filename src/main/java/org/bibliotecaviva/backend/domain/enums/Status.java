package org.bibliotecaviva.backend.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Status {
    PENDING("pending"),
    ACTIVE("active"),
    REJECTED("rejected"),
    BLOCKED("blocked");

    @JsonValue
    private final String value;

    Status(String value) {
        this.value = value;
    }
    public static Status fromString(String value) {
        return Arrays.stream(Status.values())
                .filter(s -> s.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status Inválido: " + value));
    }
}



