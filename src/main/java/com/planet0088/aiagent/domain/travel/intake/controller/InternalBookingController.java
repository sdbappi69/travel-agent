package com.planet0088.aiagent.domain.travel.intake.controller;

import com.planet0088.aiagent.domain.travel.booking.repository.BookingRepository;
import com.planet0088.aiagent.engine.tenant.utility.TenantContext;
import com.planet0088.aiagent.engine.conversation.model.ConversationMessage;
import com.planet0088.aiagent.engine.conversation.service.ConversationService;
import com.planet0088.aiagent.engine.websocket.utility.NotificationHandler;
import com.planet0088.aiagent.engine.websocket.model.NotificationMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

// Internal seam between Ops module and Chat Agent module.
// When split into separate services, this becomes a real network call
// to a different host instead of localhost. Do not let the Ops module call
// ConversationService or NotificationHandler directly — always go through
// this REST boundary, even within the same JVM.
@RestController
@RequestMapping("/api/internal/bookings")
@RequiredArgsConstructor
@Slf4j
public class InternalBookingController {

    private final ConversationService conversationService;
    private final NotificationHandler notificationHandler;
    private final BookingRepository bookingRepository;

    @Data
    static class NotifyRequest {
        private String sessionId;
        private String message;
        private String type;
    }

    @PostMapping("/{bookingId}/notify")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    public ResponseEntity<Map<String, String>> notify(@PathVariable String bookingId,
                                                      @RequestBody NotifyRequest request) {
        String tenantId = TenantContext.get();

        boolean exists = bookingRepository.findByTenantIdAndId(tenantId, bookingId).isPresent();
        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        conversationService.addMessage(tenantId, bookingId, ConversationMessage.builder()
                .role("ASSISTANT")
                .content(request.getMessage())
                .timestamp(Instant.now())
                .build());

        notificationHandler.sendToSession(request.getSessionId(), NotificationMessage.builder()
                .type(request.getType())
                .message(request.getMessage())
                .timestamp(Instant.now())
                .build());

        return ResponseEntity.ok(Map.of("status", "delivered"));
    }
}
