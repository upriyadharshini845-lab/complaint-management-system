package com.cms.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Reset token is required") String token,
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 64, message = "Password must be 8 to 64 characters") String newPassword) {
}
