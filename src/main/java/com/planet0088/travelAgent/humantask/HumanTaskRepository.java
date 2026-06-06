package com.planet0088.travelAgent.humantask;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HumanTaskRepository extends MongoRepository<HumanTask, String> {
    List<HumanTask> findByTenantIdAndStatus(String tenantId, HumanTaskStatus status);
    List<HumanTask> findByTenantIdAndBookingId(String tenantId, String bookingId);
}
