package com.cms.complaint.config;

import com.cms.complaint.entity.*;
import com.cms.complaint.enums.Priority;
import com.cms.complaint.enums.Role;
import com.cms.complaint.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * First-run data (only when the tables are empty):
 *  - departments and complaint categories
 *  - the admin account (app.admin.email / app.admin.password)
 *  - demo staff and user accounts, only when app.seed.demo-users=true (default true for development)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DepartmentRepository departments;
    private final ComplaintCategoryRepository categories;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;
    private final boolean demoUsers;

    public DataInitializer(DepartmentRepository departments, ComplaintCategoryRepository categories,
                           UserRepository users, PasswordEncoder encoder,
                           @Value("${app.admin.email:admin@cms.com}") String adminEmail,
                           @Value("${app.admin.password:Admin@123}") String adminPassword,
                           @Value("${app.seed.demo-users:true}") boolean demoUsers) {
        this.departments = departments;
        this.categories = categories;
        this.users = users;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.demoUsers = demoUsers;
    }

    @Override
    public void run(String... args) {
        if (departments.count() == 0) {
            List.of("IT Support", "Electrical & Water", "Civil & Maintenance", "Transport").forEach(n -> {
                Department d = new Department();
                d.setName(n);
                departments.save(d);
            });
        }
        if (categories.count() == 0) {
            Map<String, Priority> map = new LinkedHashMap<>();
            map.put("Technical Issues", Priority.MEDIUM);
            map.put("Infrastructure", Priority.MEDIUM);
            map.put("Water and Electricity", Priority.HIGH);
            map.put("Maintenance", Priority.LOW);
            map.put("Transportation", Priority.MEDIUM);
            map.put("Other", Priority.LOW);
            map.forEach((n, p) -> {
                ComplaintCategory c = new ComplaintCategory();
                c.setName(n);
                c.setDefaultPriority(p);
                categories.save(c);
            });
        }
        if (users.count() == 0) {
            create("Admin", adminEmail, adminPassword, Role.ADMIN, null);
            if ("Admin@123".equals(adminPassword)) {
                log.warn("The default admin password is in use. Set app.admin.password before deploying.");
            }
            if (demoUsers) {
                create("Priya S", "staff@cms.com", "Staff@123", Role.STAFF, "Electrical & Water");
                create("Arun M", "staff2@cms.com", "Staff@123", Role.STAFF, "IT Support");
                create("Ravi Kumar", "user@cms.com", "User@123", Role.USER, null);
            }
        }
    }

    private void create(String name, String email, String rawPassword, Role role, String dept) {
        User u = new User();
        u.setName(name);
        u.setEmail(email.trim().toLowerCase());
        u.setPassword(encoder.encode(rawPassword));
        u.setRole(role);
        if (dept != null) {
            u.setDepartment(departments.findByName(dept).orElse(null));
        }
        users.save(u);
    }
}
