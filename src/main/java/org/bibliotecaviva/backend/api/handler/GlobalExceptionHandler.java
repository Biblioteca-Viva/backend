package org.bibliotecaviva.backend.api.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.bibliotecaviva.backend.domain.exceptions.ApiErrorException;
import org.bibliotecaviva.backend.domain.exceptions.ApiErrorResponse;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
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

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String message = switch (ex) {
            case LockedException e -> "Conta bloqueada, contatar administrador";
            case DisabledException e -> "Conta ainda nao foi ativada";
            case BadCredentialsException e -> "Credenciais inválidas";
            default -> "Erro de autenticação desconhecido";
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
        ApiErrorResponse error = ApiErrorResponse.of(HttpStatus.BAD_REQUEST, "Campos invalidos", request.getRequestURI(), fields);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatchException(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Argumento Inválido ", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado", request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.of(status, message, request.getRequestURI(), null);
        return ResponseEntity.status(status).body(error);
    }
}
