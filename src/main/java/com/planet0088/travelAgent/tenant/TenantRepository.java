package com.planet0088.travelAgent.tenant;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TenantRepository extends MongoRepository<Tenant, String> {

    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
