package com.planet0088.aiagent.engine.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "token_usage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsage {
    @Id
    private String id;
    private String tenantId;
    private String bookingId;
    private String conversationId;
    private String model;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private double estimatedCostUsd;
    private Instant createdAt;
}
