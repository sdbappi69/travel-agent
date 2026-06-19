package com.planet0088.aiagent.engine.auth.repository;

import com.planet0088.aiagent.engine.auth.model.StaffUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StaffUserRepository extends MongoRepository<StaffUser, String> {

    Optional<StaffUser> findByTenantIdAndEmail(String tenantId, String email);

    List<StaffUser> findByTenantId(String tenantId);
}
