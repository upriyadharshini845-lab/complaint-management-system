package com.cms.complaint.service;

import com.cms.complaint.dto.AnalyticsResponse;
import com.cms.complaint.dto.AnalyticsResponse.DeptStats;
import com.cms.complaint.entity.Complaint;
import com.cms.complaint.entity.Feedback;
import com.cms.complaint.enums.ComplaintStatus;
import com.cms.complaint.repository.ComplaintRepository;
import com.cms.complaint.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ComplaintRepository complaints;
    private final FeedbackRepository feedbacks;

    public AnalyticsService(ComplaintRepository complaints, FeedbackRepository feedbacks) {
        this.complaints = complaints;
        this.feedbacks = feedbacks;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse get() {
        return compute(complaints.findAllByOrderByCreatedAtDesc(), feedbacks.findAll());
    }

    /** Pure calculation, easy to unit test. */
    public static AnalyticsResponse compute(List<Complaint> cs, List<Feedback> fb) {
        Map<String, Long> byCategory = cs.stream().collect(Collectors.groupingBy(
                c -> c.getCategory().getName(), LinkedHashMap::new, Collectors.counting()));
        Map<String, Long> byMonth = cs.stream().collect(Collectors.groupingBy(
                c -> YearMonth.from(c.getCreatedAt()).toString(), TreeMap::new, Collectors.counting()));

        long resolved = cs.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED
                || c.getStatus() == ComplaintStatus.CLOSED).count();
        long pending = cs.stream().filter(c -> c.getStatus() != ComplaintStatus.RESOLVED
                && c.getStatus() != ComplaintStatus.CLOSED && c.getStatus() != ComplaintStatus.REJECTED).count();

        Map<String, List<Complaint>> grouped = cs.stream().collect(Collectors.groupingBy(
                c -> c.getDepartment() == null ? "Unassigned" : c.getDepartment().getName(),
                LinkedHashMap::new, Collectors.toList()));
        Map<String, DeptStats> byDept = new LinkedHashMap<>();
        grouped.forEach((name, list) -> byDept.put(name, new DeptStats(list.size(),
                list.stream().filter(c -> c.getResolvedAt() != null).count(), avgHours(list))));

        int[] dist = new int[5];
        fb.forEach(f -> {
            if (f.getRating() >= 1 && f.getRating() <= 5) {
                dist[f.getRating() - 1]++;
            }
        });
        double avgRating = fb.isEmpty() ? 0 : round(fb.stream().mapToInt(Feedback::getRating).average().orElse(0), 2);

        return new AnalyticsResponse(byCategory, byMonth, pending, resolved, byDept, avgHours(cs), avgRating, dist);
    }

    private static double avgHours(List<Complaint> list) {
        return round(list.stream()
                .filter(c -> c.getResolvedAt() != null && c.getCreatedAt() != null)
                .mapToDouble(c -> Duration.between(c.getCreatedAt(), c.getResolvedAt()).toMinutes() / 60.0)
                .average().orElse(0), 1);
    }

    private static double round(double v, int digits) {
        double f = Math.pow(10, digits);
        return Math.round(v * f) / f;
    }
}
