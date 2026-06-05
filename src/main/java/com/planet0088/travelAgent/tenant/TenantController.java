package com.planet0088.travelAgent.tenant;

import com.planet0088.travelAgent.config.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterTenantRequest request) {
        Tenant tenant = tenantService.registerTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "tenantId", tenant.getId(),
            "slug", tenant.getSlug(),
            "name", tenant.getName(),
            "status", tenant.getStatus(),
            "message", "Tenant registered successfully"
        ));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> me() {
        String tenantId = TenantContext.get();
        Tenant tenant = tenantService.getByIdOrThrow(tenantId);
        return ResponseEntity.ok(Map.of(
            "tenantId", tenant.getId(),
            "slug", tenant.getSlug(),
            "name", tenant.getName(),
            "status", tenant.getStatus()
        ));
    }
}
