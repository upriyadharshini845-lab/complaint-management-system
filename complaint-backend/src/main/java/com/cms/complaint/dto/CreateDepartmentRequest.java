package com.cms.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequest(
        @NotBlank(message = "Department name is required")
        @Size(max = 100, message = "Name is too long") String name) {
}
