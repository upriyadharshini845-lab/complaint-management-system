package com.cms.complaint.repository;

import com.cms.complaint.entity.Complaint;
import com.cms.complaint.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {

    @EntityGraph(attributePaths = {"category", "department", "user", "assignedStaff"})
    Optional<Complaint> findByTrackingId(String trackingId);

    @EntityGraph(attributePaths = {"category", "department", "user", "assignedStaff"})
    List<Complaint> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"category", "department", "user", "assignedStaff"})
    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"category", "department", "user", "assignedStaff"})
    List<Complaint> findByAssignedStaffIdOrderByCreatedAtDesc(Long staffId);

    /** Paged search used by the list screens. */
    @Override
    @EntityGraph(attributePaths = {"category", "department", "user", "assignedStaff"})
    Page<Complaint> findAll(Specification<Complaint> spec, Pageable pageable);

    long countByStatus(ComplaintStatus status);

    boolean existsByDepartmentId(Long departmentId);

    /** Complaints whose deadline has passed and are not in a final status. */
    @EntityGraph(attributePaths = {"category", "department", "user", "assignedStaff"})
    @Query("select c from Complaint c where c.deadline < :now and c.status not in :finalStatuses")
    List<Complaint> findOverdue(@Param("now") LocalDateTime now,
                                @Param("finalStatuses") Collection<ComplaintStatus> finalStatuses);
}
