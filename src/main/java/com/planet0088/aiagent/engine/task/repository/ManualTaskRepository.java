package com.planet0088.aiagent.engine.task.repository;

import com.planet0088.aiagent.engine.task.model.ManualTask;
import com.planet0088.aiagent.engine.task.model.ManualTaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ManualTaskRepository extends MongoRepository<ManualTask, String> {
    List<ManualTask> findByTenantIdAndStatus(String tenantId, ManualTaskStatus status);
    List<ManualTask> findByTenantIdAndStatusIn(String tenantId, List<ManualTaskStatus> statuses);
    List<ManualTask> findByTenantIdAndBookingId(String tenantId, String bookingId);
}
