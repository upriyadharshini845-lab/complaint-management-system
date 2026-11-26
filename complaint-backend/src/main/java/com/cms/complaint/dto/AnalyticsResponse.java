package com.cms.complaint.dto;

import java.util.Map;

public record AnalyticsResponse(
        Map<String, Long> byCategory,
        Map<String, Long> byMonth,
        long pending,
        long resolved,
        Map<String, DeptStats> byDepartment,
        double avgResolutionHours,
        double avgRating,
        int[] ratingDist) {

    public record DeptStats(long total, long resolved, double avgHours) {
    }
}
