package org.bibliotecaviva.backend.api.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void typeMismatchShouldReturnBadRequestPayload() {
        HttpServletRequest request = request("/work/not-a-uuid");

        var response = handler.handleTypeMismatchException(
                new TypeMismatchException("not-a-uuid", UUID.class), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Argumento Inválido ", response.getBody().message());
        assertEquals("/work/not-a-uuid", response.getBody().path());
    }

    @Test
    void illegalArgumentShouldReturnBadRequestPayload() {
        HttpServletRequest request = request("/work/articles");

        var response = handler.handleIllegalArgumentException(new IllegalArgumentException("detail"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Argumento Inválido ", response.getBody().message());
        assertEquals("/work/articles", response.getBody().path());
    }

    private static HttpServletRequest request(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }
}
