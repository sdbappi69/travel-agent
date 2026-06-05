package com.planet0088.travelAgent.auth;

import com.planet0088.travelAgent.tenant.Tenant;
import com.planet0088.travelAgent.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final TenantService tenantService;
    private final StaffUserRepository staffUserRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

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

    public static class AuthException extends RuntimeException {
        public AuthException(String message) {
            super(message);
        }
    }
}
