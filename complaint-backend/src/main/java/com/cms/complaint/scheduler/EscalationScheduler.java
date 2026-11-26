package com.cms.complaint.scheduler;

import com.cms.complaint.service.EscalationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Runs the overdue check automatically. Interval: app.escalation.interval-ms (default 15 minutes). */
@Component
public class EscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(EscalationScheduler.class);

    private final EscalationService service;

    public EscalationScheduler(EscalationService service) {
        this.service = service;
    }

    @Scheduled(initialDelay = 60000, fixedDelayString = "${app.escalation.interval-ms:900000}")
    public void check() {
        try {
            int n = service.runCheck();
            if (n > 0) {
                log.info("Escalated {} overdue complaint(s)", n);
            }
        } catch (Exception e) {
            log.error("Escalation check failed", e);
        }
    }
}
