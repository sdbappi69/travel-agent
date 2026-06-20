package com.planet0088.aiagent.engine.task.service.implement;

import com.planet0088.aiagent.domain.travel.booking.model.Booking;
import com.planet0088.aiagent.domain.travel.booking.model.TravelerInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TripDetails;
import com.planet0088.aiagent.engine.task.model.ManualTask;
import com.planet0088.aiagent.engine.task.model.ManualTaskStatus;
import com.planet0088.aiagent.engine.task.model.ManualTaskType;
import com.planet0088.aiagent.engine.task.repository.ManualTaskRepository;
import com.planet0088.aiagent.engine.task.service.ManualTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManualTaskServiceImplement implements ManualTaskService {

    private final ManualTaskRepository manualTaskRepository;

    @Value("${travelagent.task.sla.flight-research-seconds}")
    private int flightResearchSlaSeconds;

    @Value("${travelagent.task.sla.escalation-seconds}")
    private int escalationSlaSeconds;

    @Override
    public ManualTask createFlightResearchTask(String tenantId, Booking booking) {
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

        ManualTask task = ManualTask.builder()
                .tenantId(tenantId)
                .bookingId(booking.getId())
                .type(ManualTaskType.FLIGHT_RESEARCH)
                .status(ManualTaskStatus.PENDING)
                .instructions(instructions)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .slaDeadlineAt(Instant.now().plusSeconds(flightResearchSlaSeconds))
                .build();

        return manualTaskRepository.save(task);
    }

    @Override
    public ManualTask createEscalationTask(String tenantId, String bookingId, String reason) {
        ManualTask task = ManualTask.builder()
                .tenantId(tenantId)
                .bookingId(bookingId)
                .type(ManualTaskType.ESCALATION)
                .status(ManualTaskStatus.PENDING)
                .instructions("Escalation required: " + reason)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .slaDeadlineAt(Instant.now().plusSeconds(escalationSlaSeconds))
                .build();

        return manualTaskRepository.save(task);
    }

    @Override
    public List<ManualTask> getPendingByTenant(String tenantId) {
        return manualTaskRepository.findByTenantIdAndStatus(tenantId, ManualTaskStatus.PENDING);
    }
}
