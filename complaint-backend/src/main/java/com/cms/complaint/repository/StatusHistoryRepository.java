package com.cms.complaint.repository;

import com.cms.complaint.entity.*;
import com.cms.complaint.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByComplaintIdOrderByChangedAtAsc(Long complaintId);
}
