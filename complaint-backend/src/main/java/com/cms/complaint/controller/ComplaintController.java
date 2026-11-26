package com.cms.complaint.controller;

import com.cms.complaint.dto.ComplaintRequest;
import com.cms.complaint.dto.ComplaintResponse;
import com.cms.complaint.dto.PageResponse;
import com.cms.complaint.dto.UpdateComplaintRequest;
import com.cms.complaint.security.AppUserDetails;
import com.cms.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService service;

    public ComplaintController(ComplaintService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ComplaintResponse> create(@AuthenticationPrincipal AppUserDetails p,
                                                    @Valid @RequestBody ComplaintRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p.getId(), req));
    }

    /** USER: own complaints. STAFF: assigned to them. ADMIN: all. */
    @GetMapping
    public List<ComplaintResponse> list(@AuthenticationPrincipal AppUserDetails p) {
        return service.list(p.getId(), p.getRole());
    }

    /**
     * Paged list. Example: /api/complaints/search?page=0&size=10&sort=createdAt&dir=desc&status=SUBMITTED&q=lab
     * Filters (all optional): status, priority, category, department, q (text), overdue=true
     */
    @GetMapping("/search")
    public PageResponse<ComplaintResponse> search(@AuthenticationPrincipal AppUserDetails p,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String priority,
                                                  @RequestParam(required = false) String category,
                                                  @RequestParam(required = false) String department,
                                                  @RequestParam(required = false) String q,
                                                  @RequestParam(defaultValue = "false") boolean overdue,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer size,
                                                  @RequestParam(required = false) String sort,
                                                  @RequestParam(required = false) String dir) {
        return service.search(p.getId(), p.getRole(), status, priority, category, department, q, overdue,
                page, size, sort, dir);
    }

    @GetMapping("/track/{trackingId}")
    public ComplaintResponse track(@AuthenticationPrincipal AppUserDetails p, @PathVariable String trackingId) {
        return service.track(trackingId, p.getId(), p.getRole());
    }

    /** ADMIN: assign, priority override, status. STAFF: accept, progress note, resolve (own complaints). */
    @PatchMapping("/{trackingId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ComplaintResponse update(@AuthenticationPrincipal AppUserDetails p, @PathVariable String trackingId,
                                    @RequestBody UpdateComplaintRequest req) {
        return service.update(trackingId, req, p.getId(), p.getRole());
    }
}
