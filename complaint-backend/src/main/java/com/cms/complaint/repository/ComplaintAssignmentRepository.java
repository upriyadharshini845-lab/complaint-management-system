package com.cms.complaint.repository;

import com.cms.complaint.entity.*;
import com.cms.complaint.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ComplaintAssignmentRepository extends JpaRepository<ComplaintAssignment, Long> {
    List<ComplaintAssignment> findByComplaintId(Long complaintId);
}
