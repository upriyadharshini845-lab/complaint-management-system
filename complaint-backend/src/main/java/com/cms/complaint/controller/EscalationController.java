package com.cms.complaint.controller;

import com.cms.complaint.dto.EscalationResponse;
import com.cms.complaint.service.EscalationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/escalations")
@PreAuthorize("hasRole('ADMIN')")
public class EscalationController {

    private final EscalationService service;

    public EscalationController(EscalationService service) {
        this.service = service;
    }

    @GetMapping
    public List<EscalationResponse> history() {
        return service.history();
    }

    /** Manual trigger for the same check the scheduler runs. */
    @PostMapping("/run")
    public Map<String, Integer> run() {
        return Map.of("escalated", service.runCheck());
    }
}
