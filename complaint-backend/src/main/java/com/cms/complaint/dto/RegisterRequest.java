package com.cms.complaint.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be 3 to 100 characters") String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        @Size(max = 150, message = "Email is too long") String email,
        @Pattern(regexp = "^$|[0-9]{10}", message = "Phone must be 10 digits") String phone,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be 8 to 64 characters") String password) {
}
