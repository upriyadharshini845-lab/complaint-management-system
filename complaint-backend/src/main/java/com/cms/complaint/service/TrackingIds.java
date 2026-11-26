package com.cms.complaint.service;

public final class TrackingIds {
    private TrackingIds() {
    }

    /** Example: year 2026, id 1 -> CMP-2026-1001 */
    public static String format(int year, long id) {
        return String.format("CMP-%d-%04d", year, id + 1000);
    }
}
