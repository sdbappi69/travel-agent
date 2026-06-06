package com.planet0088.travelAgent.booking;

import com.planet0088.travelAgent.humantask.HumanTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HumanTaskService humanTaskService;

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
        humanTaskService.createFlightResearchTask(tenantId, saved);
        return saved;
    }

    public Booking getByIdForTenant(String tenantId, String bookingId) {
        return bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }
}
