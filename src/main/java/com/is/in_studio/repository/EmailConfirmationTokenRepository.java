package com.is.in_studio.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.is.in_studio.entity.EmailConfirmationToken;

@Repository
public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, Long> {

    Optional<EmailConfirmationToken> findByToken(String token);

    @Query("SELECT t FROM EmailConfirmationToken t WHERE t.user.userId = :userId AND t.used = false AND t.expiresAt > :now")
    Optional<EmailConfirmationToken> findValidTokenByUserId(Long userId, Instant now);

    @Modifying
    @Query("DELETE FROM EmailConfirmationToken t WHERE t.expiresAt < :now")
    int deleteAllExpiredBefore(Instant now);
}
