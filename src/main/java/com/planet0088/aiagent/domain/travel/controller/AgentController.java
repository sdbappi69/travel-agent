package com.planet0088.aiagent.domain.travel.controller;

import com.planet0088.aiagent.domain.travel.booking.dto.BookingDetailResponse;
import com.planet0088.aiagent.domain.travel.booking.dto.BookingSummaryResponse;
import com.planet0088.aiagent.domain.travel.booking.model.Booking;
import com.planet0088.aiagent.domain.travel.booking.service.BookingService;
import com.planet0088.aiagent.domain.travel.intake.service.IntakeAgent;
import com.planet0088.aiagent.engine.tenant.utility.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/agent")
@Slf4j
public class AgentController {

    private final BookingService bookingService;
    private final IntakeAgent intakeAgent;
    private final Executor taskExecutor;

    public AgentController(BookingService bookingService,
                           IntakeAgent intakeAgent,
                           @Qualifier("agentTaskExecutor") Executor taskExecutor) {
        this.bookingService = bookingService;
        this.intakeAgent = intakeAgent;
        this.taskExecutor = taskExecutor;
    }

    @PostMapping("/session")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> createSession(@RequestBody Map<String, String> body) {
        String tenantId = TenantContext.get();
        String sessionId = body.get("sessionId");
        Booking booking = bookingService.createSession(tenantId, sessionId);
        taskExecutor.execute(() ->
                intakeAgent.sendWelcomeMessage(tenantId, booking.getId(), sessionId));
        return Map.of("bookingId", booking.getId(), "sessionId", booking.getSessionId());
    }

    // Client-facing endpoint for resuming a conversation. Requires explicit
    // tenantId since no bookingId exists yet to derive tenant from.
    // This is intentionally permissive for now (test-client.html stage);
    // revisit auth strategy when a real client-facing frontend is built.
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingSummaryResponse>> listBookings(
            @RequestParam String tenantId,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(bookingService.listBookings(tenantId, email));
    }

    // Client-facing endpoint for resuming a conversation — returns full message history.
    // Same permissive auth posture as GET /bookings and the SSE stream endpoint;
    // revisit when a real frontend exists.
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingDetailResponse> getBookingDetail(
            @PathVariable String bookingId,
            @RequestParam String tenantId) {
        return ResponseEntity.ok(bookingService.getBookingDetail(tenantId, bookingId));
    }

    @GetMapping(value = "/stream/{bookingId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String bookingId,
                             @RequestParam String message,
                             @RequestParam String sessionId) {
        SseEmitter emitter = new SseEmitter(60_000L);
        Booking booking = bookingService.getById(bookingId);
        String tenantId = booking.getTenantId();
        taskExecutor.execute(() ->
                intakeAgent.streamResponse(tenantId, bookingId, sessionId, message, emitter));
        return emitter;
    }
}
