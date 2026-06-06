package com.planet0088.travelAgent.humantask;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "human_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HumanTask {
    @Id
    private String id;
    private String tenantId;
    private String bookingId;
    private HumanTaskType type;
    private HumanTaskStatus status;
    private String instructions;
    private String responseData;
    private String resolvedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant slaDeadlineAt;
}
