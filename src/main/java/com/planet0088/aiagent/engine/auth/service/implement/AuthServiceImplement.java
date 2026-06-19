package com.planet0088.aiagent.engine.auth.service.implement;

import com.planet0088.aiagent.engine.security.service.JwtService;
import com.planet0088.aiagent.engine.auth.repository.StaffUserRepository;
import com.planet0088.aiagent.engine.auth.dto.LoginRequest;
import com.planet0088.aiagent.engine.auth.dto.LoginResponse;
import com.planet0088.aiagent.engine.auth.model.StaffUser;
import com.planet0088.aiagent.engine.auth.service.AuthException;
import com.planet0088.aiagent.engine.auth.service.AuthService;
import com.planet0088.aiagent.engine.tenant.model.Tenant;
import com.planet0088.aiagent.engine.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImplement implements AuthService {

    private final TenantService tenantService;
    private final StaffUserRepository staffUserRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        Tenant tenant = tenantService.findBySlug(request.getTenantSlug())
            .orElseThrow(() -> new AuthException("Tenant not found"));

        if ("SUSPENDED".equals(tenant.getStatus())) {
            throw new AuthException("Tenant account is suspended");
        }

        StaffUser user = staffUserRepository.findByTenantIdAndEmail(tenant.getId(), request.getEmail())
            .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new AuthException("Account is disabled");
        }

        user.setLastLoginAt(Instant.now());
        staffUserRepository.save(user);

        String token = jwtService.generateToken(
            user.getId(), user.getEmail(), tenant.getId(), tenant.getSlug(), user.getRole()
        );

        return LoginResponse.builder()
            .token(token)
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .tenantId(tenant.getId())
            .tenantSlug(tenant.getSlug())
            .build();
    }
}
