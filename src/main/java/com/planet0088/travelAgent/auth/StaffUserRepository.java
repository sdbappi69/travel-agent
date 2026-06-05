package com.planet0088.travelAgent.auth;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StaffUserRepository extends MongoRepository<StaffUser, String> {

    Optional<StaffUser> findByTenantIdAndEmail(String tenantId, String email);

    List<StaffUser> findByTenantId(String tenantId);
}
