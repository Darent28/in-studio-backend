package com.is.in_studio.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmailService {

    private final RestClient restClient;

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from}")
    private String fromAddress;

    public EmailService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://api.resend.com").build();
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        Map<String, Object> payload = Map.of(
            "from", fromAddress,
            "to", List.of(to),
            "subject", subject,
            "html", htmlBody
        );

        restClient.post()
            .uri("/emails")
            .header("Authorization", "Bearer " + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .toBodilessEntity();
    }

    public void sendHtmlEmailWithAttachment(String to, String subject, String htmlBody,
                                             String filename, byte[] fileContent) {
        Map<String, Object> attachment = Map.of(
            "filename", filename,
            "content", Base64.getEncoder().encodeToString(fileContent)
        );

        Map<String, Object> payload = Map.of(
            "from", fromAddress,
            "to", List.of(to),
            "subject", subject,
            "html", htmlBody,
            "attachments", List.of(attachment)
        );

        restClient.post()
            .uri("/emails")
            .header("Authorization", "Bearer " + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .toBodilessEntity();
    }
}
