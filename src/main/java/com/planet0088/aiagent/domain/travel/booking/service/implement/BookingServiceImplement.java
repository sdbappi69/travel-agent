package com.planet0088.aiagent.domain.travel.booking.service.implement;

import com.planet0088.aiagent.domain.travel.booking.dto.BookingDetailResponse;
import com.planet0088.aiagent.domain.travel.booking.dto.BookingSummaryResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    // WARNING: not tenant-scoped — only safe to call when the caller has no
    // other way to know the tenant yet (e.g. resolving tenant FROM the bookingId
    // itself, such as in the unauthenticated SSE stream endpoint).
    // Do not use this for any operation where tenant isolation must be enforced —
    // use getByIdForTenant(tenantId, bookingId) instead.
    @Override
    public Booking getById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
    }

    @Override
    public List<BookingSummaryResponse> listBookings(String tenantId, String email) {
        List<Booking> bookings = (email == null || email.isBlank())
                ? bookingRepository.findByTenantId(tenantId, Pageable.unpaged()).getContent()
                : bookingRepository.findByTenantIdAndClientInfoEmail(tenantId, email);

        return bookings.stream()
                .map(b -> {
                    String clientName = (b.getClientInfo() != null && b.getClientInfo().getName() != null)
                            ? b.getClientInfo().getName()
                            : "Unknown";
                    String destination = (b.getTripDetails() != null && b.getTripDetails().getDestination() != null)
                            ? b.getTripDetails().getDestination()
                            : "—";
                    String departureCity = (b.getTripDetails() != null && b.getTripDetails().getDepartureCity() != null)
                            ? b.getTripDetails().getDepartureCity()
                            : "—";
                    int messageCount = b.getMessages() == null ? 0 : b.getMessages().size();
                    return BookingSummaryResponse.builder()
                            .bookingId(b.getId())
                            .status(b.getStatus() != null ? b.getStatus().name() : null)
                            .clientName(clientName)
                            .destination(destination)
                            .departureCity(departureCity)
                            .createdAt(b.getCreatedAt())
                            .updatedAt(b.getUpdatedAt())
                            .messageCount(messageCount)
                            .build();
                })
                .sorted(Comparator.comparing(BookingSummaryResponse::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public BookingDetailResponse getBookingDetail(String tenantId, String bookingId) {
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        return BookingDetailResponse.builder()
                .bookingId(booking.getId())
                .sessionId(booking.getSessionId())
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .clientInfo(booking.getClientInfo())
                .travelerInfo(booking.getTravelerInfo())
                .tripDetails(booking.getTripDetails())
                .messages(booking.getMessages())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
