package com.cms.complaint.service;

import com.cms.complaint.dto.EscalationResponse;
import com.cms.complaint.entity.Complaint;
import com.cms.complaint.entity.Escalation;
import com.cms.complaint.enums.ComplaintStatus;
import com.cms.complaint.enums.Priority;
import com.cms.complaint.repository.ComplaintRepository;
import com.cms.complaint.repository.EscalationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Finds overdue complaints, records an escalation and alerts all admins. */
@Service
public class EscalationService {

    private static final List<ComplaintStatus> FINAL_STATUSES =
            List.of(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED, ComplaintStatus.REJECTED);

    private final ComplaintRepository complaints;
    private final EscalationRepository escalations;
    private final NotificationService notifications;

    public EscalationService(ComplaintRepository complaints, EscalationRepository escalations,
                             NotificationService notifications) {
        this.complaints = complaints;
        this.escalations = escalations;
        this.notifications = notifications;
    }

    public static String reasonFor(LocalDateTime deadline, LocalDateTime now) {
        return "Deadline exceeded by " + Duration.between(deadline, now).toHours() + "h";
    }

    /** @return number of newly escalated complaints */
    @Transactional
    public int runCheck() {
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (Complaint c : complaints.findOverdue(now, FINAL_STATUSES)) {
            if (escalations.existsByComplaintId(c.getId())) {
                continue;
            }
            Escalation e = new Escalation();
            e.setComplaint(c);
            e.setReason(reasonFor(c.getDeadline(), now));
            escalations.save(e);
            notifications.notifyAdmins("OVERDUE: " + c.getTrackingId() + " escalated (" + c.getTitle() + ")");
            count++;
        }
        return count;
    }

    @Transactional(readOnly = true)
    public List<EscalationResponse> history() {
        return escalations.findAllByOrderByEscalatedAtDesc().stream()
                .map(e -> new EscalationResponse(e.getId(), e.getComplaint().getTrackingId(), e.getComplaint().getTitle(),
                        cap(e.getComplaint().getPriority()), e.getReason(), e.getEscalatedAt()))
                .toList();
    }

    private static String cap(Priority p) {
        return p.name().charAt(0) + p.name().substring(1).toLowerCase();
    }
}
