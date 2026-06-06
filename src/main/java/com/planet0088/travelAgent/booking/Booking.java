package com.planet0088.travelAgent.booking;

import com.planet0088.travelAgent.conversation.ConversationMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    private String id;

    @Indexed
    private String tenantId;

    private String sessionId;
    private BookingStatus status;
    private ClientInfo clientInfo;
    private TravelerInfo travelerInfo;
    private TripDetails tripDetails;
    private List<ConversationMessage> messages;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
