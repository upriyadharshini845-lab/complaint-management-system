package com.cms.complaint.service;

import com.cms.complaint.dto.AnalyticsResponse;
import com.cms.complaint.entity.Complaint;
import com.cms.complaint.entity.ComplaintCategory;
import com.cms.complaint.entity.Department;
import com.cms.complaint.entity.Feedback;
import com.cms.complaint.enums.ComplaintStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyticsServiceTest {

    private static Complaint complaint(String category, String dept, ComplaintStatus status,
                                       LocalDateTime created, LocalDateTime resolved) {
        Complaint c = new Complaint();
        ComplaintCategory cat = new ComplaintCategory();
        cat.setName(category);
        c.setCategory(cat);
        if (dept != null) {
            Department d = new Department();
            d.setName(dept);
            c.setDepartment(d);
        }
        c.setStatus(status);
        c.setCreatedAt(created);
        c.setResolvedAt(resolved);
        return c;
    }

    private static Feedback feedback(int rating) {
        Feedback f = new Feedback();
        f.setRating(rating);
        return f;
    }

    @Test
    void computesCountsAveragesAndRatings() {
        LocalDateTime t0 = LocalDateTime.of(2026, 11, 1, 9, 0);
        Complaint a = complaint("Technical Issues", "IT Support", ComplaintStatus.RESOLVED, t0, t0.plusHours(10));
        Complaint b = complaint("Technical Issues", "IT Support", ComplaintStatus.RESOLVED, t0, t0.plusHours(20));
        Complaint d = complaint("Infrastructure", null, ComplaintStatus.SUBMITTED, t0, null);

        AnalyticsResponse r = AnalyticsService.compute(List.of(a, b, d), List.of(feedback(5), feedback(3)));

        assertEquals(2L, r.byCategory().get("Technical Issues"));
        assertEquals(3L, r.byMonth().get("2026-11"));
        assertEquals(1L, r.pending());
        assertEquals(2L, r.resolved());
        assertEquals(15.0, r.avgResolutionHours(), 0.01);
        assertEquals(15.0, r.byDepartment().get("IT Support").avgHours(), 0.01);
        assertEquals(1L, r.byDepartment().get("Unassigned").total());
        assertEquals(4.0, r.avgRating(), 0.01);
        assertEquals(1, r.ratingDist()[4]);
        assertEquals(1, r.ratingDist()[2]);
    }

    @Test
    void emptyDataGivesZeros() {
        AnalyticsResponse r = AnalyticsService.compute(List.of(), List.of());
        assertEquals(0L, r.pending());
        assertEquals(0.0, r.avgResolutionHours(), 0.01);
        assertEquals(0.0, r.avgRating(), 0.01);
    }
}
