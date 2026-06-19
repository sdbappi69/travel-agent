package com.planet0088.aiagent.engine.task.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "manual_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualTask {
    @Id
    private String id;
    private String tenantId;
    private String bookingId;
    private ManualTaskType type;
    private ManualTaskStatus status;
    private String instructions;
    private String responseData;
    private String previewMessage;
    private String resolvedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant slaDeadlineAt;
}
