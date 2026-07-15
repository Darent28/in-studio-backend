package com.is.in_studio.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.is.in_studio.entity.Membership;
import com.is.in_studio.repository.MembershipRepository;

@Service
public class MembershipExpirationService {

    private static final Logger log = LoggerFactory.getLogger(MembershipExpirationService.class);
    private final MembershipRepository membershipRepository;
    private final ExcelReportService excelReportService;
    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${resend.report-recipient}")
    private String reportRecipient;

    public MembershipExpirationService(MembershipRepository membershipRepository,
                                        ExcelReportService excelReportService,
                                        EmailService emailService) {
        this.membershipRepository = membershipRepository;
        this.excelReportService = excelReportService;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOverdueMemberships() {
        int count = membershipRepository.expireOverdue(LocalDate.now());
        if (count > 0) {
            log.info("Expired {} membership(s) past their end date", count);
        }
    }

    @Transactional
    public void expireAndReport() {
        expireOverdueMemberships();

        List<Membership> expired = membershipRepository.findByStatusWithUser(Membership.MembershipStatus.EXPIRED);
        if (expired.isEmpty()) {
            log.info("No expired memberships to report.");
            return;
        }

        byte[] xlsx;
        try {
            xlsx = excelReportService.buildExpiredMembershipsReport(expired);
        } catch (IOException e) {
            log.error("Failed to build expired memberships Excel report", e);
            return;
        }

        YearMonth ym = YearMonth.now();
        String filename = String.format("%02d-%d-expired-memberships.xlsx", ym.getMonthValue(), ym.getYear());
        String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
              <h2 style="color:#5b21b6;">Expired Memberships Report</h2>
              <p>Please find attached the expired memberships report for <strong>%s %d</strong>.</p>
              <p style="font-size:15px;">Total expired memberships: <strong>%d</strong></p>
              <hr style="border:none;border-top:2px solid #a78bfa;margin:24px 0;">
              <p style="color:#9ca3af;font-size:12px;">In Studio — automated report</p>
            </div>
            """.formatted(monthName, ym.getYear(), expired.size());

        try {
            emailService.sendHtmlEmailWithAttachment(reportRecipient,
                "Expired Memberships Report — " + monthName + " " + ym.getYear(),
                html, filename, xlsx);
            log.info("Sent expired memberships report ({} rows) to {}", expired.size(), reportRecipient);
        } catch (Exception e) {
            log.error("Failed to send expired memberships report email", e);
        }
    }
}
