package com.cms.complaint.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackingIdsTest {
    @Test
    void formatsTrackingId() {
        assertEquals("CMP-2026-1001", TrackingIds.format(2026, 1));
        assertEquals("CMP-2026-1042", TrackingIds.format(2026, 42));
    }
}
