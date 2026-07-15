package com.is.in_studio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.is.in_studio.service.EmailConfirmationService;
import com.is.in_studio.service.MembershipExpirationService;
import com.is.in_studio.service.PasswordResetService;

@RestController
@RequestMapping("/api/cron")
public class CronController {

    @Value("${app.cron-secret}")
    private String cronSecret;

    private final MembershipExpirationService membershipExpirationService;
    private final EmailConfirmationService emailConfirmationService;
    private final PasswordResetService passwordResetService;

    public CronController(MembershipExpirationService membershipExpirationService,
                          EmailConfirmationService emailConfirmationService,
                          PasswordResetService passwordResetService) {
        this.membershipExpirationService = membershipExpirationService;
        this.emailConfirmationService = emailConfirmationService;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/{secret}/expire-memberships")
    public String expireMemberships(@PathVariable String secret) {
        if (!cronSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        membershipExpirationService.expireOverdueMemberships();
        return "ok";
    }

    @DeleteMapping("/{secret}/expire-generated-tokens")
    public String expireGeneratedTokens(@PathVariable String secret) {
        if (!cronSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        int deleted = emailConfirmationService.purgeExpiredTokens()
                    + passwordResetService.purgeExpiredTokens();
        return "deleted " + deleted;
    }

    @GetMapping("/{secret}/expire-memberships-report")
    public String expireMembershipsReport(@PathVariable String secret) {
        if (!cronSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        membershipExpirationService.expireAndReport();
        return "ok";
    }
}
