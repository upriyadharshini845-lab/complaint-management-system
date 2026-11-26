package com.cms.complaint.dto;

import java.time.LocalDateTime;

public record FeedbackResponse(Long id, String trackingId, String title, Long userId, int rating, String comment,
                               LocalDateTime createdAt) {
}
