package com.planet0088.aiagent.engine.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryResponse {
    private String taskId;
    private String bookingId;
    private String type;
    private String status;
    private String instructions;
    private String clientName;
    private String destination;
    private Instant createdAt;
    private Instant slaDeadlineAt;
}
