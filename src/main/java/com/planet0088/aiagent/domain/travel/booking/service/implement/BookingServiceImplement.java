package com.planet0088.aiagent.domain.travel.booking.service.implement;

import com.planet0088.aiagent.domain.travel.booking.model.Booking;
import com.planet0088.aiagent.domain.travel.booking.model.BookingStatus;
import com.planet0088.aiagent.domain.travel.booking.model.ClientInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TravelerInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TripDetails;
import com.planet0088.aiagent.domain.travel.booking.repository.BookingRepository;
import com.planet0088.aiagent.domain.travel.booking.service.BookingService;
import com.planet0088.aiagent.engine.task.service.ManualTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImplement implements BookingService {

    private final BookingRepository bookingRepository;
    private final ManualTaskService manualTaskService;

    @Override
    public Booking createSession(String tenantId, String sessionId) {
        Booking booking = Booking.builder()
                .tenantId(tenantId)
                .sessionId(sessionId)
                .status(BookingStatus.INQUIRY)
                .messages(new ArrayList<>())
                .createdBy("AGENT")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return bookingRepository.save(booking);
    }

    @Override
    public Booking finalizeIntake(String tenantId, String bookingId,
                                  ClientInfo clientInfo,
                                  TravelerInfo travelerInfo,
                                  TripDetails tripDetails) {
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        booking.setClientInfo(clientInfo);
        booking.setTravelerInfo(travelerInfo);
        booking.setTripDetails(tripDetails);
        booking.setStatus(BookingStatus.RESEARCH);
        booking.setUpdatedAt(Instant.now());

        Booking saved = bookingRepository.save(booking);
        manualTaskService.createFlightResearchTask(tenantId, saved);
        return saved;
    }

    @Override
    public Booking getByIdForTenant(String tenantId, String bookingId) {
        return bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }
}
