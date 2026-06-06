package com.planet0088.travelAgent.humantask;

import com.planet0088.travelAgent.booking.Booking;
import com.planet0088.travelAgent.booking.TripDetails;
import com.planet0088.travelAgent.booking.TravelerInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HumanTaskService {

    private final HumanTaskRepository humanTaskRepository;

    public HumanTask createFlightResearchTask(String tenantId, Booking booking) {
        TripDetails trip = booking.getTripDetails();
        TravelerInfo travelers = booking.getTravelerInfo();

        String instructions = "Flight research required.\n" +
                "Trip Type: " + trip.getTripType() + "\n" +
                "Route: " + trip.getDepartureCity() + " → " + trip.getDestination() + "\n" +
                "Departure: " + trip.getDepartureDate() + " | Return: " + trip.getReturnDate() + "\n" +
                "Travelers: " + travelers.getAdults() + " adults, " + travelers.getChildren() + " children\n" +
                "Nationalities: " + travelers.getNationalities() + "\n" +
                "Class: " + trip.getFlightPreference() + "\n" +
                "Budget: " + trip.getBudgetAmount() + " " + trip.getBudgetCurrency() + "\n" +
                "Visa Required: " + trip.isVisaRequired() + "\n" +
                "Special Requests: " + trip.getSpecialRequests() + "\n" +
                "Please research available flight options and enter results in the ops portal.";

        HumanTask task = HumanTask.builder()
                .tenantId(tenantId)
                .bookingId(booking.getId())
                .type(HumanTaskType.FLIGHT_RESEARCH)
                .status(HumanTaskStatus.PENDING)
                .instructions(instructions)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .slaDeadlineAt(Instant.now().plusSeconds(4 * 3600))
                .build();

        return humanTaskRepository.save(task);
    }

    public HumanTask createEscalationTask(String tenantId, String bookingId, String reason) {
        HumanTask task = HumanTask.builder()
                .tenantId(tenantId)
                .bookingId(bookingId)
                .type(HumanTaskType.ESCALATION)
                .status(HumanTaskStatus.PENDING)
                .instructions("Escalation required: " + reason)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .slaDeadlineAt(Instant.now().plusSeconds(3600))
                .build();

        return humanTaskRepository.save(task);
    }

    public List<HumanTask> getPendingByTenant(String tenantId) {
        return humanTaskRepository.findByTenantIdAndStatus(tenantId, HumanTaskStatus.PENDING);
    }
}
