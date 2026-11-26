package com.cms.complaint.security;

import com.cms.complaint.entity.User;
import com.cms.complaint.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Logged-in user as Spring Security sees it. Use @AuthenticationPrincipal in controllers. */
public class AppUserDetails implements UserDetails {
    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;
    private final boolean active;

    public AppUserDetails(User u) {
        this.id = u.getId();
        this.name = u.getName();
        this.email = u.getEmail();
        this.password = u.getPassword();
        this.role = u.getRole();
        this.active = u.isActive();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Role getRole() { return role; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isEnabled() { return active; }
}
