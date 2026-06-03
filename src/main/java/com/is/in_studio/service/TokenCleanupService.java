package com.is.in_studio.service;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.is.in_studio.repository.EmailConfirmationTokenRepository;

@Service
public class TokenCleanupService {

    private final EmailConfirmationTokenRepository tokenRepository;

    public TokenCleanupService(EmailConfirmationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteExpiredTokens() {
        tokenRepository.deleteAllExpiredBefore(Instant.now());
    }
}
