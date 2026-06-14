package com.is.in_studio.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.is.in_studio.repository.MembershipRepository;

@Service
public class MembershipExpirationService {

    private static final Logger log = LoggerFactory.getLogger(MembershipExpirationService.class);

    private final MembershipRepository membershipRepository;

    public MembershipExpirationService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOverdueMemberships() {
        int count = membershipRepository.expireOverdue(LocalDate.now());
        if (count > 0) {
            log.info("Expired {} membership(s) past their end date", count);
        }
    }
}
