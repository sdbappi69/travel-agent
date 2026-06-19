package com.planet0088.aiagent.engine.tenant.service;

import com.planet0088.aiagent.engine.tenant.dto.RegisterTenantRequest;
import com.planet0088.aiagent.engine.tenant.model.Tenant;

import java.util.Optional;

public interface TenantService {

    Tenant registerTenant(RegisterTenantRequest request);

    Optional<Tenant> findBySlug(String slug);

    Tenant getBySlugOrThrow(String slug);

    Tenant getByIdOrThrow(String id);

    Tenant getByTenantId(String tenantId);
}
