package com.cms.complaint.service;

import com.cms.complaint.dto.*;
import com.cms.complaint.entity.User;
import com.cms.complaint.enums.Role;
import com.cms.complaint.exception.ApiException;
import com.cms.complaint.repository.UserRepository;
import com.cms.complaint.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    /** Public registration always creates a USER. Admin and staff are created by an admin. */
    @Transactional
    public UserResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("Email already registered");
        }
        User u = new User();
        u.setName(req.name().trim());
        u.setEmail(email);
        u.setPhone(req.phone());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(Role.USER);
        return UserResponse.from(users.save(u));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User u = users.findByEmail(req.email().trim().toLowerCase())
                .filter(x -> encoder.matches(req.password(), x.getPassword()))
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!u.isActive()) {
            throw ApiException.forbidden("Account is disabled. Contact the administrator.");
        }
        return new AuthResponse(jwtService.generateToken(u), UserResponse.from(u));
    }
}
