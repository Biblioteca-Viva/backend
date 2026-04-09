package org.bibliotecaviva.backend.domain.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        @Schema(description = "Only presents in validations errors, otherwise wont show in the payload")
        List<FieldError> invalidFields
) {
    public static ApiErrorResponse of(HttpStatus status, String message, String path, List<FieldError> fields) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fields
        );
    }

    public record FieldError(String field, String message) {
    }
}
