package com.planet0088.aiagent.engine.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffRoleHelper {

    private final SecurityProperties securityProperties;

    public boolean isStaff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> securityProperties.getRoles().getStaff().stream()
                        .anyMatch(role -> a.getAuthority().equals("ROLE_" + role)));
    }
}
