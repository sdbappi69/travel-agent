package com.planet0088.aiagent.domain.travel.booking.dto;

import com.planet0088.aiagent.domain.travel.booking.model.ClientInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TravelerInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TripDetails;
import com.planet0088.aiagent.engine.conversation.model.ConversationMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {
    private String bookingId;
    private String sessionId;
    private String status;
    private ClientInfo clientInfo;
    private TravelerInfo travelerInfo;
    private TripDetails tripDetails;
    private List<ConversationMessage> messages;
    private Instant createdAt;
    private Instant updatedAt;
}
