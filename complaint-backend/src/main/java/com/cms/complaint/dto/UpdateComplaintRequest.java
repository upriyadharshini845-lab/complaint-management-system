package com.cms.complaint.dto;

/** All fields optional. ADMIN may send any of them. STAFF may send only status, note, resolutionNotes. */
public record UpdateComplaintRequest(String status, String note, String priority, String department,
                                     Long staffId, String resolutionNotes) {
}
