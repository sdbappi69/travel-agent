package com.planet0088.travelAgent.tenant;

import com.planet0088.travelAgent.auth.StaffUser;
import com.planet0088.travelAgent.auth.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;

    public Tenant registerTenant(RegisterTenantRequest request) {
        if (tenantRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Slug already taken: " + request.getSlug());
        }

        Tenant tenant = Tenant.builder()
            .slug(request.getSlug())
            .name(request.getName())
            .ownerEmail(request.getOwnerEmail())
            .status("ACTIVE")
            .settings(Tenant.TenantSettings.builder()
                .timezone(request.getTimezone())
                .currency(request.getCurrency())
                .agentName(request.getAgentName())
                .build())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Tenant saved = tenantRepository.save(tenant);
        seedOwnerAccount(saved.getId(), saved.getSlug(), request.getOwnerEmail(), request.getOwnerPassword());

        log.info("Tenant registered: {} with id {}", saved.getSlug(), saved.getId());
        return saved;
    }

    private void seedOwnerAccount(String tenantId, String tenantSlug, String email, String rawPassword) {
        StaffUser owner = StaffUser.builder()
            .tenantId(tenantId)
            .tenantSlug(tenantSlug)
            .email(email)
            .passwordHash(passwordEncoder.encode(rawPassword))
            .role("OWNER")
            .active(true)
            .createdAt(Instant.now())
            .build();
        staffUserRepository.save(owner);
    }

    public Optional<Tenant> findBySlug(String slug) {
        return tenantRepository.findBySlug(slug);
    }

    public Tenant getBySlugOrThrow(String slug) {
        return tenantRepository.findBySlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + slug));
    }

    public Tenant getByIdOrThrow(String id) {
        return tenantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    public Tenant getByTenantId(String tenantId) {
        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }
}
