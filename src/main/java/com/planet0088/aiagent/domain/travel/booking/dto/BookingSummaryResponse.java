package com.planet0088.aiagent.domain.travel.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSummaryResponse {
    private String bookingId;
    private String status;
    private String clientName;
    private String destination;
    private String departureCity;
    private Instant createdAt;
    private Instant updatedAt;
    private int messageCount;
}
