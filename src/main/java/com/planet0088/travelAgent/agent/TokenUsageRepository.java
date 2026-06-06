package com.planet0088.travelAgent.agent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TokenUsageRepository extends MongoRepository<TokenUsage, String> {
    Page<TokenUsage> findByTenantId(String tenantId, Pageable pageable);
    List<TokenUsage> findByTenantIdAndBookingId(String tenantId, String bookingId);
}
