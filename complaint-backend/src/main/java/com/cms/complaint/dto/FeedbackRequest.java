package com.cms.complaint.dto;

import jakarta.validation.constraints.*;

public record FeedbackRequest(
        @NotBlank(message = "Tracking ID is required") String trackingId,
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5") int rating,
        @Size(max = 500, message = "Comment must be under 500 characters") String comment) {
}
