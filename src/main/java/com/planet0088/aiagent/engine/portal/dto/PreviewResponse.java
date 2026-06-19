package com.planet0088.aiagent.engine.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewResponse {
    private String taskId;
    private String formattedMessage;
}
