package com.cms.complaint.service;

import com.cms.complaint.dto.CreateUserRequest;
import com.cms.complaint.dto.UpdateProfileRequest;
import com.cms.complaint.dto.UserResponse;
import com.cms.complaint.entity.Department;
import com.cms.complaint.entity.User;
import com.cms.complaint.exception.ApiException;
import com.cms.complaint.repository.DepartmentRepository;
import com.cms.complaint.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository users;
    private final DepartmentRepository departments;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, DepartmentRepository departments, PasswordEncoder encoder) {
        this.users = users;
        this.departments = departments;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long id) {
        return UserResponse.from(find(id));
    }

    @Transactional
    public UserResponse updateProfile(Long id, UpdateProfileRequest req) {
        User u = find(id);
        u.setName(req.name().trim());
        u.setPhone(req.phone());
        if (req.password() != null && !req.password().isBlank()) {
            if (req.password().length() < 8 || req.password().length() > 64) {
                throw ApiException.badRequest("Password must be 8 to 64 characters");
            }
            u.setPassword(encoder.encode(req.password()));
        }
        return UserResponse.from(users.save(u));
    }

    // ---------- admin ----------

    @Transactional(readOnly = true)
    public List<UserResponse> listAll() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("Email already exists");
        }
        Department dept = null;
        if (req.department() != null && !req.department().isBlank()) {
            dept = departments.findByName(req.department())
                    .orElseThrow(() -> ApiException.badRequest("Invalid department"));
        }
        User u = new User();
        u.setName(req.name().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password()));
        u.setRole(req.role());
        u.setDepartment(dept);
        return UserResponse.from(users.save(u));
    }

    @Transactional
    public void toggleActive(Long id, Long currentAdminId) {
        if (id.equals(currentAdminId)) {
            throw ApiException.badRequest("You cannot disable your own account");
        }
        User u = find(id);
        u.setActive(!u.isActive());
        users.save(u);
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
