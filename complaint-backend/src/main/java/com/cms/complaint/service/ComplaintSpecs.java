package com.cms.complaint.service;

import com.cms.complaint.entity.Complaint;
import com.cms.complaint.enums.ComplaintStatus;
import com.cms.complaint.enums.Priority;
import com.cms.complaint.enums.Role;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Builds the WHERE part of the complaint search (role scope + filters + text search). */
public final class ComplaintSpecs {

    private static final List<ComplaintStatus> FINAL_STATUSES =
            List.of(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED, ComplaintStatus.REJECTED);

    private ComplaintSpecs() {
    }

    public static Specification<Complaint> of(Long userId, Role role, ComplaintStatus status, Priority priority,
                                              String category, String department, String q, boolean overdue) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // Role scope: USER sees own, STAFF sees assigned, ADMIN sees everything.
            if (role == Role.USER) {
                ps.add(cb.equal(root.get("user").get("id"), userId));
            } else if (role == Role.STAFF) {
                ps.add(cb.equal(root.get("assignedStaff").get("id"), userId));
            }
            if (status != null) {
                ps.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                ps.add(cb.equal(root.get("priority"), priority));
            }
            if (category != null && !category.isBlank()) {
                ps.add(cb.equal(root.get("category").get("name"), category.trim()));
            }
            if (department != null && !department.isBlank()) {
                ps.add(cb.equal(root.get("department").get("name"), department.trim()));
            }
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("trackingId")), like),
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("location")), like)));
            }
            if (overdue) {
                ps.add(cb.lessThan(root.<LocalDateTime>get("deadline"), LocalDateTime.now()));
                ps.add(cb.not(root.get("status").in(FINAL_STATUSES)));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
