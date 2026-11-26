package com.cms.complaint.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EscalationServiceTest {
    @Test
    void reasonShowsWholeHoursOverdue() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 22, 18, 0);
        assertEquals("Deadline exceeded by 5h", EscalationService.reasonFor(now.minusHours(5).minusMinutes(10), now));
        assertEquals("Deadline exceeded by 0h", EscalationService.reasonFor(now.minusMinutes(20), now));
    }
}
