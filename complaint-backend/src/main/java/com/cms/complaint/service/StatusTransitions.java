package com.cms.complaint.service;

import com.cms.complaint.enums.ComplaintStatus;
import com.cms.complaint.enums.Role;

/** Who can move a complaint from one status to another. */
public final class StatusTransitions {
    private StatusTransitions() {
    }

    public static boolean canChange(Role role, ComplaintStatus from, ComplaintStatus to) {
        if (from == to) {
            return false;
        }
        return switch (role) {
            // Admin can move freely, except out of the final states CLOSED and REJECTED.
            case ADMIN -> from != ComplaintStatus.CLOSED && from != ComplaintStatus.REJECTED;
            // Staff: accept (ASSIGNED -> IN_PROGRESS) and resolve (IN_PROGRESS -> RESOLVED) only.
            case STAFF -> (from == ComplaintStatus.ASSIGNED && to == ComplaintStatus.IN_PROGRESS)
                    || (from == ComplaintStatus.IN_PROGRESS && to == ComplaintStatus.RESOLVED);
            default -> false;
        };
    }
}
