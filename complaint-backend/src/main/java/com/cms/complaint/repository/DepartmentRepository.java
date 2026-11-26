package com.cms.complaint.repository;

import com.cms.complaint.entity.*;
import com.cms.complaint.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
    boolean existsByName(String name);
}
