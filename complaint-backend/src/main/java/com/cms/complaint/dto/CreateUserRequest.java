package com.cms.complaint.dto;

import com.cms.complaint.enums.Role;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be 3 to 100 characters") String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email") String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be 8 to 64 characters") String password,
        @NotNull(message = "Role is required") Role role,
        String department) {
}
