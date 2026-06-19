package com.planet0088.aiagent.engine.portal.service;

import com.planet0088.aiagent.domain.travel.flight.model.FlightResearchSubmission;
import com.planet0088.aiagent.engine.portal.dto.PreviewResponse;
import com.planet0088.aiagent.engine.portal.dto.TaskDetailResponse;
import com.planet0088.aiagent.engine.portal.dto.TaskSummaryResponse;
import com.planet0088.aiagent.engine.task.model.ManualTask;

import java.util.List;

public interface TaskService {

    List<TaskSummaryResponse> getPendingTasks(String tenantId);

    TaskDetailResponse getTaskDetail(String tenantId, String taskId);

    ManualTask submitResolution(String tenantId, String taskId,
                                FlightResearchSubmission submission,
                                String resolvedByStaffId);

    PreviewResponse generatePreview(String tenantId, String taskId);

    void confirmSend(String tenantId, String taskId);

    String getSessionIdForTask(String tenantId, String taskId);
}
