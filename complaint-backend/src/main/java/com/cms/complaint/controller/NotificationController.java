package com.cms.complaint.controller;

import com.cms.complaint.dto.NotificationResponse;
import com.cms.complaint.security.AppUserDetails;
import com.cms.complaint.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal AppUserDetails p) {
        return service.list(p.getId());
    }

    @PatchMapping("/read-all")
    public Map<String, String> readAll(@AuthenticationPrincipal AppUserDetails p) {
        service.markAllRead(p.getId());
        return Map.of("message", "All notifications marked as read");
    }
}
