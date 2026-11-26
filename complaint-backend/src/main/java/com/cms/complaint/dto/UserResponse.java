package com.cms.complaint.dto;

import com.cms.complaint.entity.User;
import com.cms.complaint.enums.Role;

/** Safe view of a user. The password is never included. */
public record UserResponse(Long id, String name, String email, String phone, Role role, String department, boolean active) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole(),
                u.getDepartment() == null ? null : u.getDepartment().getName(), u.isActive());
    }
}
