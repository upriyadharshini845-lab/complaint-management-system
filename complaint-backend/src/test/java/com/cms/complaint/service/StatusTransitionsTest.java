package com.cms.complaint.service;

import com.cms.complaint.enums.ComplaintStatus;
import com.cms.complaint.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusTransitionsTest {

    @Test
    void staffCanAcceptAndResolve() {
        assertTrue(StatusTransitions.canChange(Role.STAFF, ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS));
        assertTrue(StatusTransitions.canChange(Role.STAFF, ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED));
    }

    @Test
    void staffCannotSkipOrReject() {
        assertFalse(StatusTransitions.canChange(Role.STAFF, ComplaintStatus.ASSIGNED, ComplaintStatus.RESOLVED));
        assertFalse(StatusTransitions.canChange(Role.STAFF, ComplaintStatus.IN_PROGRESS, ComplaintStatus.REJECTED));
    }

    @Test
    void adminCanMoveFreelyExceptFromFinalStates() {
        assertTrue(StatusTransitions.canChange(Role.ADMIN, ComplaintStatus.SUBMITTED, ComplaintStatus.UNDER_REVIEW));
        assertTrue(StatusTransitions.canChange(Role.ADMIN, ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED));
        assertFalse(StatusTransitions.canChange(Role.ADMIN, ComplaintStatus.CLOSED, ComplaintStatus.IN_PROGRESS));
        assertFalse(StatusTransitions.canChange(Role.ADMIN, ComplaintStatus.REJECTED, ComplaintStatus.ASSIGNED));
    }

    @Test
    void sameStatusIsNotAChangeAndUsersCannotChange() {
        assertFalse(StatusTransitions.canChange(Role.ADMIN, ComplaintStatus.ASSIGNED, ComplaintStatus.ASSIGNED));
        assertFalse(StatusTransitions.canChange(Role.USER, ComplaintStatus.SUBMITTED, ComplaintStatus.CLOSED));
    }
}
