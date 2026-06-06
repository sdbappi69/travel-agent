package com.planet0088.travelAgent.agent;

import com.planet0088.travelAgent.booking.Booking;
import com.planet0088.travelAgent.booking.BookingService;
import com.planet0088.travelAgent.config.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @GetMapping(value = "/stream/{bookingId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String bookingId,
                             @RequestParam String message,
                             @RequestParam String sessionId,
                             @RequestParam String tenantId) {
        SseEmitter emitter = new SseEmitter(60_000L);
        bookingService.getByIdForTenant(tenantId, bookingId);
        taskExecutor.execute(() ->
                intakeAgent.streamResponse(tenantId, bookingId, sessionId, message, emitter));
        return emitter;
    }
}
