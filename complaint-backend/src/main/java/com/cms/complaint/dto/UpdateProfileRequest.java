package com.cms.complaint.dto;

import jakarta.validation.constraints.*;

public record UpdateProfileRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be 3 to 100 characters") String name,
        @Pattern(regexp = "^$|[0-9]{10}", message = "Phone must be 10 digits") String phone,
        String password) {
}
