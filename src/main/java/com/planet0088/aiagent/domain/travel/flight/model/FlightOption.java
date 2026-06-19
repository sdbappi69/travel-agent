package com.planet0088.aiagent.domain.travel.flight.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightOption {
    private String airline;
    private String flightNumber;
    private String departureTime;
    private String arrivalTime;
    private double price;
    private String currency;
    private int stops;
    private String notes;
}
