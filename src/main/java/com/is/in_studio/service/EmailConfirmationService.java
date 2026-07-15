package com.is.in_studio.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.is.in_studio.config.AppProperties;
import com.is.in_studio.entity.EmailConfirmationToken;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.ProcessServiceException;
import com.is.in_studio.repository.EmailConfirmationTokenRepository;
import com.is.in_studio.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class EmailConfirmationService {

    private static final Logger log = LoggerFactory.getLogger(EmailConfirmationService.class);

    private final UserRepository userRepository;
    private final EmailConfirmationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;

    public EmailConfirmationService(UserRepository userRepository, EmailConfirmationTokenRepository tokenRepository,
                                     EmailService emailService, AppProperties appProperties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.appProperties = appProperties;
    }

    @Transactional
    public int purgeExpiredTokens() {
        return tokenRepository.deleteAllExpiredBefore(Instant.now());
    }

    @Transactional
    public int processUnverifiedUsers() {
        List<User> unverified = userRepository.findUnverifiedActiveUsers();
        int sent = 0;

        for (User user : unverified) {
            Optional<EmailConfirmationToken> existing = tokenRepository
                .findValidTokenByUserId(user.getUserId(), Instant.now());
            if (existing.isPresent()) continue;

            sendConfirmationEmail(user);
            sent++;
        }
        return sent;
    }

    @Transactional
    public void sendConfirmationEmail(User user) {
        String tokenValue = UUID.randomUUID().toString();
        EmailConfirmationToken token = new EmailConfirmationToken(
            tokenValue,
            user,
            Instant.now().plus(appProperties.getConfirmationTokenExpiryHours(), ChronoUnit.HOURS)
        );
        tokenRepository.save(token);

        String confirmUrl = appProperties.getBaseUrl() + "/api/auth/confirm-email?token=" + tokenValue;
        String html = buildConfirmationHtml(user.getFirstName(), confirmUrl);

        try {
            emailService.sendHtmlEmail(user.getEmail(), "Confirm your email - In Studio", html);
        } catch (Exception e) {
            log.error("Failed to send confirmation email to user id={}", user.getUserId(), e);
        }
    }

    @Transactional
    public void confirmEmail(String tokenValue) {
        EmailConfirmationToken token = tokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new ProcessServiceException("Invalid confirmation token."));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ProcessServiceException("This confirmation link has expired.");
        }

        userRepository.markEmailVerified(token.getUser().getUserId());
        tokenRepository.delete(token);
    }

    private String buildConfirmationHtml(String firstName, String confirmUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #333;">Welcome, %s!</h2>
                <p>Thank you for registering with In Studio. Please confirm your email address by clicking the button below:</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s"
                       style="background-color: #4CAF50; color: white; padding: 14px 28px;
                              text-decoration: none; border-radius: 6px; font-size: 16px;
                              display: inline-block;">
                        Confirm Email
                    </a>
                </div>
                <p style="color: #666; font-size: 14px;">This link expires in 24 hours. If you did not create this account, you can ignore this email.</p>
                <p style="color: #666; font-size: 12px;">If the button doesn't work, copy this link into your browser:<br>%s</p>
            </body>
            </html>
            """.formatted(firstName, confirmUrl, confirmUrl);
    }
}
