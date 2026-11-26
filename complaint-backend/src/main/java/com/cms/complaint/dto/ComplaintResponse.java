package com.cms.complaint.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Shape matches what the frontend expects. Priority is "Critical/High/Medium/Low", status is e.g. IN_PROGRESS. */
public record ComplaintResponse(
        String trackingId, String title, String description, String category, String location,
        String urgency, String priority, boolean priorityOverridden, String status,
        Long userId, String userName, String department, Long staffId, String staffName,
        String resolutionNotes, LocalDateTime deadline, LocalDateTime createdAt, LocalDateTime updatedAt,
        LocalDateTime resolvedAt, String imageData, List<HistoryEntry> history) {

    public record HistoryEntry(String status, String note, String by, LocalDateTime at) {
    }
}
