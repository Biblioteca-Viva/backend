package org.bibliotecaviva.backend.application.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.bibliotecaviva.backend.domain.exceptions.EmailDeliveryException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResendEmailServiceTest {

    @Test
    void sendPasswordResetCodeShouldBuildExpectedEmailWithoutNetworkAccess() throws Exception {
        try (MockedConstruction<Resend> construction = mockConstruction(Resend.class)) {
            ResendEmailService service = new ResendEmailService("re_test", "Biblioteca <no-reply@test.com>");
            Resend client = construction.constructed().getFirst();
            Emails emails = mock(Emails.class);
            when(client.emails()).thenReturn(emails);
            when(emails.send(any(CreateEmailOptions.class))).thenReturn(new CreateEmailResponse("email-id"));

            service.sendPasswordResetCode("student@test.com", "01234567");

            ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
            verify(emails).send(captor.capture());
            CreateEmailOptions options = captor.getValue();
            assertEquals("Biblioteca <no-reply@test.com>", options.getFrom());
            assertEquals(List.of("student@test.com"), options.getTo());
            assertEquals("Código para redefinir sua senha", options.getSubject());
            assertTrue(options.getText().contains("01234567"));
            assertTrue(options.getText().contains("10 minutos"));
            assertTrue(options.getHtml().contains("01234567"));
            assertTrue(options.getHtml().contains("10 minutos"));
        }
    }

    @Test
    void sendPasswordResetCodeShouldTranslateProviderFailure() throws Exception {
        try (MockedConstruction<Resend> construction = mockConstruction(Resend.class)) {
            ResendEmailService service = new ResendEmailService("re_test", "no-reply@test.com");
            Resend client = construction.constructed().getFirst();
            Emails emails = mock(Emails.class);
            when(client.emails()).thenReturn(emails);
            when(emails.send(any(CreateEmailOptions.class)))
                    .thenThrow(new ResendException(503, "provider unavailable"));

            assertThrows(EmailDeliveryException.class,
                    () -> service.sendPasswordResetCode("student@test.com", "12345678"));
        }
    }
}
