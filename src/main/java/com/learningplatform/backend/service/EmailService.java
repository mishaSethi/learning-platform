package com.learningplatform.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Sends emails through Brevo. Sending runs in the background (@Async), so a
 * request like login answers straight away instead of waiting for Brevo.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;
    private final String fromEmail;

    public EmailService(@Value("${brevo.api-key}") String apiKey,
                        @Value("${mail.from}") String fromEmail) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
        this.fromEmail = fromEmail;
    }

    @Async
    public void sendPasswordResetCode(String toEmail, String code) {
        sendEmail(toEmail, "Your password reset code",
                "<p>Your password reset code is:</p>"
                        + "<h2 style=\"letter-spacing:4px\">" + code + "</h2>"
                        + "<p>It expires in 15 minutes.</p>"
                        + "<p>If you didn't ask to reset your password, you can ignore this email.</p>");
    }

    @Async
    public void sendLoginCode(String toEmail, String code) {
        sendEmail(toEmail, "Your login code",
                "<p>Your login verification code is:</p>"
                        + "<h2 style=\"letter-spacing:4px\">" + code + "</h2>"
                        + "<p>It expires in 5 minutes.</p>"
                        + "<p>If you didn't just try to log in, someone may know your password. "
                        + "Please reset it straight away.</p>");
    }

    private void sendEmail(String toEmail, String subject, String html) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "Learning Platform", "email", fromEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", subject,
                "htmlContent", html
        );

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // Runs in the background, so nobody else will see this error - log it
            // (never log the code itself)
            log.error("Failed to send '{}' email to {}: {}", subject, toEmail, e.getMessage());
        }
    }
}
