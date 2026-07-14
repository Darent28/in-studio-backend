package com.is.in_studio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.is.in_studio.service.EmailConfirmationService;
import com.is.in_studio.service.MembershipExpirationService;

@RestController
@RequestMapping("/api/cron")
public class CronController {

    @Value("${app.cron-secret}")
    private String cronSecret;

    private final MembershipExpirationService membershipExpirationService;
    private final EmailConfirmationService emailConfirmationService;

    public CronController(MembershipExpirationService membershipExpirationService,
                          EmailConfirmationService emailConfirmationService) {
        this.membershipExpirationService = membershipExpirationService;
        this.emailConfirmationService = emailConfirmationService;
    }

    @GetMapping("/{secret}/expire-memberships")
    public String expireMemberships(@PathVariable String secret) {
        if (!cronSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        membershipExpirationService.expireOverdueMemberships();
        return "ok";
    }

    @DeleteMapping("/{secret}/expire-email-tokens")
    public String purgeExpiredTokens(@PathVariable String secret) {
        if (!cronSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        int deleted = emailConfirmationService.purgeExpiredTokens();
        return "deleted " + deleted;
    }
}
