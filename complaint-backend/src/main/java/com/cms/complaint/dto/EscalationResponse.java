package com.cms.complaint.dto;

import java.time.LocalDateTime;

public record EscalationResponse(Long id, String trackingId, String title, String priority, String reason,
                                 LocalDateTime escalatedAt) {
}
