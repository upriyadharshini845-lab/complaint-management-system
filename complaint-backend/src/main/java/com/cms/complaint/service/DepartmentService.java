package com.cms.complaint.service;

import com.cms.complaint.dto.DepartmentResponse;
import com.cms.complaint.entity.Department;
import com.cms.complaint.exception.ApiException;
import com.cms.complaint.repository.ComplaintRepository;
import com.cms.complaint.repository.DepartmentRepository;
import com.cms.complaint.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departments;
    private final UserRepository users;
    private final ComplaintRepository complaints;

    public DepartmentService(DepartmentRepository departments, UserRepository users, ComplaintRepository complaints) {
        this.departments = departments;
        this.users = users;
        this.complaints = complaints;
    }

    public List<DepartmentResponse> list() {
        return departments.findAll().stream().map(d -> new DepartmentResponse(d.getId(), d.getName())).toList();
    }

    @Transactional
    public DepartmentResponse create(String name) {
        String n = name.trim();
        if (departments.existsByName(n)) {
            throw ApiException.conflict("Department already exists");
        }
        Department d = new Department();
        d.setName(n);
        d = departments.save(d);
        return new DepartmentResponse(d.getId(), d.getName());
    }

    @Transactional
    public void delete(Long id) {
        Department d = departments.findById(id).orElseThrow(() -> ApiException.notFound("Department not found"));
        if (users.existsByDepartmentId(id) || complaints.existsByDepartmentId(id)) {
            throw ApiException.conflict("Department is in use and cannot be deleted");
        }
        departments.delete(d);
    }
}
