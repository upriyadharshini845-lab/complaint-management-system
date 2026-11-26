package com.cms.complaint.repository;

import com.cms.complaint.entity.Escalation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EscalationRepository extends JpaRepository<Escalation, Long> {

    @EntityGraph(attributePaths = "complaint")
    List<Escalation> findAllByOrderByEscalatedAtDesc();

    boolean existsByComplaintId(Long complaintId);
}
