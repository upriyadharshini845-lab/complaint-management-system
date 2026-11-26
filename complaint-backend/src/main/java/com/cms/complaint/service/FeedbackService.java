package com.cms.complaint.service;

import com.cms.complaint.dto.FeedbackRequest;
import com.cms.complaint.dto.FeedbackResponse;
import com.cms.complaint.entity.Complaint;
import com.cms.complaint.entity.Feedback;
import com.cms.complaint.entity.User;
import com.cms.complaint.enums.ComplaintStatus;
import com.cms.complaint.enums.Role;
import com.cms.complaint.exception.ApiException;
import com.cms.complaint.repository.ComplaintRepository;
import com.cms.complaint.repository.FeedbackRepository;
import com.cms.complaint.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbacks;
    private final ComplaintRepository complaints;
    private final UserRepository users;
    private final NotificationService notifications;

    public FeedbackService(FeedbackRepository feedbacks, ComplaintRepository complaints, UserRepository users,
                           NotificationService notifications) {
        this.feedbacks = feedbacks;
        this.complaints = complaints;
        this.users = users;
        this.notifications = notifications;
    }

    @Transactional
    public FeedbackResponse create(Long userId, FeedbackRequest req) {
        Complaint c = complaints.findByTrackingId(req.trackingId().trim().toUpperCase())
                .orElseThrow(() -> ApiException.notFound("Complaint not found"));
        if (!c.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You can give feedback only for your own complaints");
        }
        if (c.getStatus() != ComplaintStatus.RESOLVED && c.getStatus() != ComplaintStatus.CLOSED) {
            throw ApiException.badRequest("Feedback can be given only after the complaint is resolved");
        }
        if (feedbacks.existsByComplaintId(c.getId())) {
            throw ApiException.conflict("Feedback already submitted for this complaint");
        }
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        Feedback f = new Feedback();
        f.setComplaint(c);
        f.setUser(user);
        f.setRating(req.rating());
        f.setComment(req.comment() == null || req.comment().isBlank() ? null : req.comment().trim());
        f = feedbacks.save(f);
        notifications.notifyAdmins("Feedback " + req.rating() + "/5 received for " + c.getTrackingId());
        return toResponse(f);
    }

    /** ADMIN sees all feedback, everyone else sees only their own. */
    @Transactional(readOnly = true)
    public List<FeedbackResponse> list(Long userId, Role role) {
        List<Feedback> list = role == Role.ADMIN
                ? feedbacks.findAllByOrderByCreatedAtDesc()
                : feedbacks.findByUserIdOrderByCreatedAtDesc(userId);
        return list.stream().map(this::toResponse).toList();
    }

    private FeedbackResponse toResponse(Feedback f) {
        return new FeedbackResponse(f.getId(), f.getComplaint().getTrackingId(), f.getComplaint().getTitle(),
                f.getUser().getId(), f.getRating(), f.getComment(), f.getCreatedAt());
    }
}
