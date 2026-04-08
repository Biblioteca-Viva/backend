package org.bibliotecaviva.backend.api.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.bibliotecaviva.backend.domain.exceptions.ApiErrorException;
import org.bibliotecaviva.backend.domain.exceptions.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice

public class GlobalExceptionHandler {

    @ExceptionHandler(ApiErrorException.class)
    public ResponseEntity<ApiErrorResponse> handleApiErrorException(ApiErrorException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getMessage(), request);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountStatusException(AccountStatusException ex, HttpServletRequest request) {
        String message = switch (ex){
            case LockedException e -> "Conta bloqueada, contatar administrador";
            case DisabledException e -> "Conta ainda nao foi ativada";
            default -> ex.getMessage();
        };
        return build(HttpStatus.FORBIDDEN, message, request);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiErrorResponse.FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        ApiErrorResponse error = ApiErrorResponse.of(HttpStatus.BAD_REQUEST, "Invalid Fields", request.getRequestURI(), fields);
        return ResponseEntity.badRequest().body(error);
    }

    //Todo: Implement exceptions handlers for specific exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        // Return a generic error response
        return ResponseEntity.status(500).body("An unexpected error occurred: " + ex.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(status, message, request.getRequestURI(), null);
        return ResponseEntity.status(status).body(error);
    }
}
