package com.cms.complaint.repository;

import com.cms.complaint.entity.Feedback;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @EntityGraph(attributePaths = {"complaint", "user"})
    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"complaint", "user"})
    List<Feedback> findAllByOrderByCreatedAtDesc();

    boolean existsByComplaintId(Long complaintId);
}
