package com.cms.complaint.repository;

import com.cms.complaint.entity.*;
import com.cms.complaint.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ComplaintCategoryRepository extends JpaRepository<ComplaintCategory, Long> {
    Optional<ComplaintCategory> findByName(String name);
}
