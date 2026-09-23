package com.tiagofsps.hunting_fishing_store.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Best-effort mail delivery. Failures are logged and swallowed so checkout,
 * registration and password-reset flows never fail because of an SMTP outage.
 * When no SMTP host (or sender address) is configured the service runs in a
 * disabled, log-only mode so local dev and test runs work without a mail
 * server.
 */
@Slf4j
@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final boolean enabled;

    public MailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${app.mail.from:}") String from) {
        this.enabled = mailHost != null && !mailHost.isBlank() && from != null && !from.isBlank();
        this.mailSender = enabled ? mailSenderProvider.getIfAvailable() : null;
        this.from = from;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void send(String to, String subject, String body) {
        if (!enabled || mailSender == null) {
            log.info("[mail disabled] would send to={} subject={}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            mailSender.send(message);
            log.debug("Sent email to={} subject={}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to={} subject={}: {}", to, subject, e.getMessage(), e);
        }
    }
}