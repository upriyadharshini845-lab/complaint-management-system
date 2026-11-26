package com.cms.complaint.controller;

import com.cms.complaint.dto.FeedbackRequest;
import com.cms.complaint.dto.FeedbackResponse;
import com.cms.complaint.security.AppUserDetails;
import com.cms.complaint.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FeedbackResponse> create(@AuthenticationPrincipal AppUserDetails p,
                                                   @Valid @RequestBody FeedbackRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p.getId(), req));
    }

    @GetMapping
    public List<FeedbackResponse> list(@AuthenticationPrincipal AppUserDetails p) {
        return service.list(p.getId(), p.getRole());
    }
}
