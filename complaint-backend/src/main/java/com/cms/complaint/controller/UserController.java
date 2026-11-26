package com.cms.complaint.controller;

import com.cms.complaint.dto.CreateUserRequest;
import com.cms.complaint.dto.UpdateProfileRequest;
import com.cms.complaint.dto.UserResponse;
import com.cms.complaint.security.AppUserDetails;
import com.cms.complaint.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal AppUserDetails principal,
                                 @Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateProfile(principal.getId(), req);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> list() {
        return userService.listAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(req));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> toggle(@AuthenticationPrincipal AppUserDetails principal, @PathVariable Long id) {
        userService.toggleActive(id, principal.getId());
        return Map.of("message", "User status updated");
    }
}
