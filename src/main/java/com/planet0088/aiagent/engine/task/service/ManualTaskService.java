package com.planet0088.aiagent.engine.task.service;

import com.planet0088.aiagent.domain.travel.booking.model.Booking;
import com.planet0088.aiagent.engine.task.model.ManualTask;

import java.util.List;

public interface ManualTaskService {

    ManualTask createFlightResearchTask(String tenantId, Booking booking);

    ManualTask createEscalationTask(String tenantId, String bookingId, String reason);

    List<ManualTask> getPendingByTenant(String tenantId);
}
