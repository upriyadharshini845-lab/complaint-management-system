package com.cms.complaint.controller;

import com.cms.complaint.repository.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Quick check that the server and the database both work. */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final UserRepository users;
    private final DepartmentRepository departments;
    private final ComplaintCategoryRepository categories;

    public HealthController(UserRepository users, DepartmentRepository departments, ComplaintCategoryRepository categories) {
        this.users = users;
        this.departments = departments;
        this.categories = categories;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", "UP");
        m.put("time", LocalDateTime.now().toString());
        m.put("users", users.count());
        m.put("departments", departments.count());
        m.put("categories", categories.count());
        return m;
    }
}
