package com.cms.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplaintRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 150, message = "Title must be 5 to 150 characters") String title,
        @NotBlank(message = "Category is required") String category,
        String urgency,
        String department,
        @NotBlank(message = "Location is required")
        @Size(max = 150, message = "Location is too long") String location,
        @NotBlank(message = "Description is required")
        @Size(min = 20, max = 2000, message = "Description must be 20 to 2000 characters") String description,
        String imageData) {
}
