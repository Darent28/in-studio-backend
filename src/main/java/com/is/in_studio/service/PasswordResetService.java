package com.is.in_studio.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.is.in_studio.entity.PasswordResetToken;
import com.is.in_studio.entity.User;
import com.is.in_studio.exception.CustomExceptions.ProcessServiceException;
import com.is.in_studio.repository.PasswordResetTokenRepository;
import com.is.in_studio.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_EXPIRY_MINUTES = 60;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    public PasswordResetService(UserRepository userRepository,
                                 PasswordResetTokenRepository tokenRepository,
                                 EmailService emailService,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public int purgeExpiredTokens() {
        return tokenRepository.deleteAllExpiredBefore(Instant.now());
    }

    @Transactional
    public void sendResetEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal whether the email exists
            return;
        }
        User user = userOpt.get();

        // Invalidate any previous tokens for this user
        tokenRepository.deleteAllByUserId(user.getUserId());

        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken(
            tokenValue,
            user,
            Instant.now().plus(TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES)
        );
        tokenRepository.save(token);

        String resetUrl = baseUrl + "/api/auth/reset-password?token=" + tokenValue;
        String html = buildResetEmailHtml(user.getFirstName(), resetUrl);

        try {
            emailService.sendHtmlEmail(user.getEmail(), "Reset your password — In Studio", html);
        } catch (Exception e) {
            log.error("Failed to send password reset email to user id={}", user.getUserId(), e);
        }
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new ProcessServiceException("Password must be at least 8 characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ProcessServiceException("Passwords do not match.");
        }

        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new ProcessServiceException("Invalid or expired reset link."));

        if (token.getUsed()) {
            throw new ProcessServiceException("This reset link has already been used.");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ProcessServiceException("This reset link has expired. Please request a new one.");
        }

        userRepository.updatePassword(token.getUser().getUserId(), passwordEncoder.encode(newPassword));
        token.setUsed(true);
        tokenRepository.save(token);
    }

    private String buildResetEmailHtml(String firstName, String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background-color:#f3f0ff;font-family:Arial,sans-serif;">
              <div style="max-width:520px;margin:40px auto;background:#ffffff;border-radius:12px;
                          box-shadow:0 4px 20px rgba(109,40,217,0.12);padding:48px 40px;">
                <h2 style="color:#5b21b6;margin:0 0 8px;">Reset your password</h2>
                <p style="color:#6b7280;margin:0 0 28px;">Hi %s, we received a request to reset the password for your In Studio account.</p>
                <div style="text-align:center;margin:0 0 28px;">
                  <a href="%s"
                     style="background-color:#7c3aed;color:#ffffff;padding:14px 32px;
                            text-decoration:none;border-radius:8px;font-size:15px;
                            font-weight:bold;display:inline-block;">
                    Reset Password
                  </a>
                </div>
                <p style="color:#9ca3af;font-size:13px;margin:0 0 8px;">This link expires in 60 minutes.</p>
                <p style="color:#9ca3af;font-size:13px;margin:0;">If you didn't request a password reset, you can safely ignore this email.</p>
                <hr style="border:none;border-top:2px solid #ede9fe;margin:28px 0 16px;">
                <p style="color:#c4b5fd;font-size:12px;margin:0;">If the button doesn't work, copy this link:<br>%s</p>
              </div>
            </body>
            </html>
            """.formatted(firstName, resetUrl, resetUrl);
    }
}
