package org.bibliotecaviva.backend.application.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.log4j.Log4j2;
import org.bibliotecaviva.backend.domain.exceptions.EmailDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ResendEmailService {

    private final Resend resend;
    private final String fromEmail;

    public ResendEmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String fromEmail
    ) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    public void sendPasswordResetCode(String recipient, String code) {
        String text = "Seu código para redefinir a senha é " + code
                + ". Ele expira em 10 minutos. Se voce não fez esta solicitação, ignore este email.";
        String html = """
                <!doctype html>
                <html lang="pt-BR">
                  <body style="font-family: Arial, sans-serif; color: #202124;">
                    <h2>Redefinição de senha</h2>
                    <p>Use o código abaixo para redefinir sua senha:</p>
                    <p style="font-size: 28px; font-weight: bold; letter-spacing: 6px;">%s</p>
                    <p>O código expira em 10 minutos.</p>
                    <p>Se você não fez esta solicitação, ignore este email.</p>
                  </body>
                </html>
                """.formatted(code);

        CreateEmailOptions email = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(recipient)
                .subject("Código para redefinir sua senha")
                .text(text)
                .html(html)
                .build();

        try {
            resend.emails().send(email);
        } catch (ResendException exception) {
            log.error(
                    "Falha ao enviar email de redefinicão. status={}",
                    exception.getStatusCode()
            );
            throw new EmailDeliveryException();
        }
    }
}
