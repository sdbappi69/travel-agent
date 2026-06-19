package com.planet0088.aiagent.engine.portal.dto;

import com.planet0088.aiagent.domain.travel.booking.model.ClientInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TravelerInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TripDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailResponse {
    private String taskId;
    private String bookingId;
    private String type;
    private String status;
    private String instructions;
    private String responseData;
    private String previewMessage;
    private ClientInfo clientInfo;
    private TravelerInfo travelerInfo;
    private TripDetails tripDetails;
    private Instant createdAt;
    private Instant slaDeadlineAt;
}
