package com.planet0088.aiagent.domain.travel.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDetails {
    private TripType tripType;
    private String departureCity;
    private String destination;
    private String flightPreference;
    private String budgetAmount;
    private String budgetCurrency;
    private String specialRequests;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private boolean visaRequired;
    private boolean travelInsurance;
    private boolean airportTransfer;
}
