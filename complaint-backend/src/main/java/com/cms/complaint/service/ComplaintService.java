package com.cms.complaint.service;

import com.cms.complaint.dto.ComplaintRequest;
import com.cms.complaint.dto.ComplaintResponse;
import com.cms.complaint.dto.ComplaintResponse.HistoryEntry;
import com.cms.complaint.dto.UpdateComplaintRequest;
import com.cms.complaint.entity.*;
import com.cms.complaint.enums.ComplaintStatus;
import com.cms.complaint.enums.Priority;
import com.cms.complaint.enums.Role;
import com.cms.complaint.exception.ApiException;
import com.cms.complaint.repository.*;
import com.cms.complaint.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ComplaintService {

    private final ComplaintRepository complaints;
    private final UserRepository users;
    private final ComplaintCategoryRepository categories;
    private final DepartmentRepository departments;
    private final StatusHistoryRepository histories;
    private final ComplaintAttachmentRepository attachments;
    private final ComplaintAssignmentRepository assignments;
    private final PriorityCalculator priorityCalculator;
    private final SlaPolicy sla;
    private final FileStorageService files;
    private final NotificationService notifications;

    public ComplaintService(ComplaintRepository complaints, UserRepository users, ComplaintCategoryRepository categories,
                            DepartmentRepository departments, StatusHistoryRepository histories,
                            ComplaintAttachmentRepository attachments, ComplaintAssignmentRepository assignments,
                            PriorityCalculator priorityCalculator, SlaPolicy sla, FileStorageService files,
                            NotificationService notifications) {
        this.complaints = complaints;
        this.users = users;
        this.categories = categories;
        this.departments = departments;
        this.histories = histories;
        this.attachments = attachments;
        this.assignments = assignments;
        this.priorityCalculator = priorityCalculator;
        this.sla = sla;
        this.files = files;
        this.notifications = notifications;
    }

    @Transactional
    public ComplaintResponse create(Long userId, ComplaintRequest req) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        ComplaintCategory category = categories.findByName(req.category())
                .orElseThrow(() -> ApiException.badRequest("Invalid category"));
        Department department = null;
        if (req.department() != null && !req.department().isBlank()) {
            department = departments.findByName(req.department())
                    .orElseThrow(() -> ApiException.badRequest("Invalid department"));
        }
        Priority urgency = parsePriority(req.urgency());
        Priority priority = priorityCalculator.calculate(category.getDefaultPriority(), urgency,
                req.title() + " " + req.description());

        Complaint c = new Complaint();
        c.setTrackingId("T" + UUID.randomUUID().toString().replace("-", "").substring(0, 20)); // temporary
        c.setTitle(req.title().trim());
        c.setDescription(req.description().trim());
        c.setLocation(req.location().trim());
        c.setCategory(category);
        c.setDepartment(department);
        c.setUser(user);
        c.setUrgency(urgency);
        c.setPriority(priority);
        c.setStatus(ComplaintStatus.SUBMITTED);
        c.setDeadline(LocalDateTime.now().plusHours(sla.hoursFor(priority)));
        c = complaints.save(c);
        c.setTrackingId(TrackingIds.format(LocalDate.now().getYear(), c.getId()));

        addHistory(c, ComplaintStatus.SUBMITTED, "Complaint submitted", user);

        if (req.imageData() != null && !req.imageData().isBlank()) {
            FileStorageService.StoredFile f = files.saveDataUrl(req.imageData());
            ComplaintAttachment a = new ComplaintAttachment();
            a.setComplaint(c);
            a.setFileName(f.originalName());
            a.setFilePath(f.storedName());
            a.setContentType(f.contentType());
            attachments.save(a);
        }

        notifications.notify(user, "Complaint " + c.getTrackingId() + " submitted successfully");
        notifications.notifyAdmins("New complaint " + c.getTrackingId() + " (" + cap(priority) + ")");
        return toResponse(c, historyOf(c), null);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> list(Long userId, Role role) {
        List<Complaint> list = switch (role) {
            case ADMIN -> complaints.findAllByOrderByCreatedAtDesc();
            case STAFF -> complaints.findByAssignedStaffIdOrderByCreatedAtDesc(userId);
            default -> complaints.findByUserIdOrderByCreatedAtDesc(userId);
        };
        return list.stream().map(c -> toResponse(c, List.of(), null)).toList();
    }

    /** Server-side pagination, sorting, filtering and search. Scope depends on the role. */
    @Transactional(readOnly = true)
    public PageResponse<ComplaintResponse> search(Long userId, Role role, String status, String priority,
                                                  String category, String department, String q, boolean overdue,
                                                  Integer page, Integer size, String sort, String dir) {
        ComplaintStatus st = isBlank(status) ? null : parseStatus(status);
        Priority pr = isBlank(priority) ? null : parsePriority(priority);
        Sort sorting = Sort.by(PagingUtil.direction(dir), PagingUtil.sortField(sort))
                .and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(PagingUtil.page(page), PagingUtil.size(size), sorting);
        Page<Complaint> result = complaints.findAll(
                ComplaintSpecs.of(userId, role, st, pr, category, department, q, overdue), pageable);
        return PageResponse.of(result.map(c -> toResponse(c, List.of(), null)));
    }

    @Transactional(readOnly = true)
    public ComplaintResponse track(String trackingId, Long userId, Role role) {
        Complaint c = complaints.findByTrackingId(trackingId.trim().toUpperCase())
                .orElseThrow(() -> ApiException.notFound("Complaint not found"));
        if (role == Role.USER && !c.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("You can view only your own complaints");
        }
        if (role == Role.STAFF && !isAssignedTo(c, userId)) {
            throw ApiException.forbidden("This complaint is not assigned to you");
        }
        return toResponse(c, historyOf(c), imageOf(c));
    }

    /**
     * ADMIN: assign department/staff, override priority, change status.
     * STAFF (own assigned complaints only): accept, add progress note, resolve with notes.
     */
    @Transactional
    public ComplaintResponse update(String trackingId, UpdateComplaintRequest req, Long userId, Role role) {
        Complaint c = complaints.findByTrackingId(trackingId.trim().toUpperCase())
                .orElseThrow(() -> ApiException.notFound("Complaint not found"));
        User actor = users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));

        if (role == Role.USER) {
            throw ApiException.forbidden("Users cannot update complaints");
        }
        if (role == Role.STAFF) {
            if (!isAssignedTo(c, userId)) {
                throw ApiException.forbidden("This complaint is not assigned to you");
            }
            if (req.priority() != null || req.staffId() != null || !isBlank(req.department())) {
                throw ApiException.forbidden("Staff cannot change priority or assignment");
            }
            if (c.getStatus() != ComplaintStatus.ASSIGNED && c.getStatus() != ComplaintStatus.IN_PROGRESS) {
                throw ApiException.badRequest("This complaint can no longer be updated");
            }
        }

        List<String> notes = new ArrayList<>();
        ComplaintStatus oldStatus = c.getStatus();
        ComplaintStatus newStatus = isBlank(req.status()) ? null : parseStatus(req.status());

        if (!isBlank(req.priority())) {
            Priority p = parsePriority(req.priority());
            if (p != c.getPriority()) {
                c.setPriority(p);
                c.setPriorityOverridden(true);
                c.setDeadline(c.getCreatedAt().plusHours(sla.hoursFor(p)));
                notes.add("Priority set to " + cap(p));
            }
        }

        if (!isBlank(req.department())) {
            Department d = departments.findByName(req.department().trim())
                    .orElseThrow(() -> ApiException.badRequest("Invalid department"));
            if (c.getDepartment() == null || !c.getDepartment().getId().equals(d.getId())) {
                c.setDepartment(d);
                notes.add("Department: " + d.getName());
            }
        }

        boolean assignedNow = false;
        if (req.staffId() != null && !isAssignedTo(c, req.staffId())) {
            User staff = users.findById(req.staffId())
                    .filter(u -> u.getRole() == Role.STAFF && u.isActive())
                    .orElseThrow(() -> ApiException.badRequest("Selected staff is not valid"));
            c.setAssignedStaff(staff);
            if (c.getDepartment() == null && staff.getDepartment() != null) {
                c.setDepartment(staff.getDepartment());
            }
            ComplaintAssignment a = new ComplaintAssignment();
            a.setComplaint(c);
            a.setDepartment(c.getDepartment());
            a.setStaff(staff);
            a.setAssignedBy(actor);
            assignments.save(a);
            notes.add("Assigned to " + staff.getName());
            notifications.notify(staff, "Complaint " + c.getTrackingId() + " assigned to you");
            assignedNow = true;
        }

        // Assigning a staff member moves a new complaint to ASSIGNED automatically.
        if (assignedNow && (oldStatus == ComplaintStatus.SUBMITTED || oldStatus == ComplaintStatus.UNDER_REVIEW)
                && (newStatus == null || newStatus == oldStatus)) {
            newStatus = ComplaintStatus.ASSIGNED;
        }

        if (!isBlank(req.resolutionNotes())) {
            c.setResolutionNotes(req.resolutionNotes().trim());
        }
        if (!isBlank(req.note())) {
            notes.add(req.note().trim());
        }

        boolean statusChanged = false;
        if (newStatus != null && newStatus != oldStatus) {
            if (!StatusTransitions.canChange(role, oldStatus, newStatus)) {
                throw ApiException.badRequest("Cannot change status from " + oldStatus + " to " + newStatus);
            }
            if (newStatus == ComplaintStatus.RESOLVED && isBlank(c.getResolutionNotes())) {
                throw ApiException.badRequest("Resolution notes are required to resolve a complaint");
            }
            c.setStatus(newStatus);
            statusChanged = true;
            if (newStatus == ComplaintStatus.RESOLVED) {
                c.setResolvedAt(LocalDateTime.now());
            } else if (newStatus != ComplaintStatus.CLOSED) {
                c.setResolvedAt(null);
            }
            notifications.notify(c.getUser(), "Complaint " + c.getTrackingId() + " is now "
                    + newStatus.name().replace('_', ' '));
            if (newStatus == ComplaintStatus.RESOLVED) {
                notifications.notify(c.getUser(), "Complaint " + c.getTrackingId() + " is resolved. Please give your feedback.");
            }
        }

        if (statusChanged || !notes.isEmpty()) {
            String text = String.join(". ", notes);
            addHistory(c, c.getStatus(), text.length() > 500 ? text.substring(0, 500) : text, actor);
        }
        complaints.save(c);
        return toResponse(c, historyOf(c), imageOf(c));
    }

    // ---------- helpers ----------

    private boolean isAssignedTo(Complaint c, Long userId) {
        return c.getAssignedStaff() != null && c.getAssignedStaff().getId().equals(userId);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void addHistory(Complaint c, ComplaintStatus status, String note, User by) {
        StatusHistory h = new StatusHistory();
        h.setComplaint(c);
        h.setStatus(status);
        h.setNote(note);
        h.setChangedBy(by);
        histories.save(h);
    }

    private List<HistoryEntry> historyOf(Complaint c) {
        return histories.findByComplaintIdOrderByChangedAtAsc(c.getId()).stream()
                .map(h -> new HistoryEntry(h.getStatus().name(), h.getNote(),
                        h.getChangedBy() == null ? "System" : h.getChangedBy().getName(), h.getChangedAt()))
                .toList();
    }

    private String imageOf(Complaint c) {
        return attachments.findByComplaintId(c.getId()).stream().findFirst()
                .map(a -> files.readAsDataUrl(a.getFilePath(), a.getContentType())).orElse(null);
    }

    private ComplaintResponse toResponse(Complaint c, List<HistoryEntry> history, String imageData) {
        User staff = c.getAssignedStaff();
        return new ComplaintResponse(c.getTrackingId(), c.getTitle(), c.getDescription(), c.getCategory().getName(),
                c.getLocation(), cap(c.getUrgency()), cap(c.getPriority()), c.isPriorityOverridden(), c.getStatus().name(),
                c.getUser().getId(), c.getUser().getName(),
                c.getDepartment() == null ? null : c.getDepartment().getName(),
                staff == null ? null : staff.getId(), staff == null ? null : staff.getName(),
                c.getResolutionNotes(), c.getDeadline(), c.getCreatedAt(), c.getUpdatedAt(), c.getResolvedAt(),
                imageData, history);
    }

    private static Priority parsePriority(String s) {
        if (s == null || s.isBlank()) {
            return Priority.MEDIUM;
        }
        try {
            return Priority.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid priority");
        }
    }

    private static ComplaintStatus parseStatus(String s) {
        try {
            return ComplaintStatus.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid status");
        }
    }

    private static String cap(Priority p) {
        String n = p.name();
        return n.charAt(0) + n.substring(1).toLowerCase();
    }
}
