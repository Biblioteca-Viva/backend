package org.bibliotecaviva.backend.api.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    //Todo: Implement exceptions handlers for specific exceptions

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {
        System.err.println("An error occurred: " + ex.getMessage());
        return "An unexpected error occurred. Please try again later.";
        }
}
