package com.planet0088.aiagent.engine.tenant.repository;

import com.planet0088.aiagent.engine.tenant.model.Tenant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TenantRepository extends MongoRepository<Tenant, String> {

    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
