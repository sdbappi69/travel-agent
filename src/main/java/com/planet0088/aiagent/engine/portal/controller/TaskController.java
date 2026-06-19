package com.planet0088.aiagent.engine.portal.controller;

import com.planet0088.aiagent.engine.portal.dto.PreviewResponse;
import com.planet0088.aiagent.engine.portal.dto.TaskDetailResponse;
import com.planet0088.aiagent.engine.portal.dto.TaskSummaryResponse;
import com.planet0088.aiagent.engine.portal.service.TaskService;
import com.planet0088.aiagent.engine.tenant.utility.TenantContext;
import com.planet0088.aiagent.domain.travel.flight.model.FlightResearchSubmission;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;
    private final RestTemplate restTemplate;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    public ResponseEntity<List<TaskSummaryResponse>> getPendingTasks() {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(taskService.getPendingTasks(tenantId));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    public ResponseEntity<TaskDetailResponse> getTaskDetail(@PathVariable String taskId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(taskService.getTaskDetail(tenantId, taskId));
    }

    @PostMapping("/{taskId}/resolve")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    public ResponseEntity<Object> resolve(@PathVariable String taskId,
                                          @RequestBody FlightResearchSubmission submission) {
        String tenantId = TenantContext.get();
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>)
                SecurityContextHolder.getContext().getAuthentication().getDetails();
        String staffUserId = (String) details.get("userId");

        return ResponseEntity.ok(taskService.submitResolution(tenantId, taskId, submission, staffUserId));
    }

    @PostMapping("/{taskId}/preview")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    public ResponseEntity<PreviewResponse> preview(@PathVariable String taskId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(taskService.generatePreview(tenantId, taskId));
    }

    @PostMapping("/{taskId}/send")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    public ResponseEntity<Map<String, String>> send(@PathVariable String taskId,
                                                     HttpServletRequest request) {
        String tenantId = TenantContext.get();

        try {
            taskService.confirmSend(tenantId, taskId);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        String sessionId = taskService.getSessionIdForTask(tenantId, taskId);
        TaskDetailResponse taskDetail = taskService.getTaskDetail(tenantId, taskId);

        Map<String, String> notifyBody = Map.of(
                "sessionId", sessionId,
                "message", taskDetail.getPreviewMessage(),
                "type", "AGENT_MESSAGE"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        }

        String notifyUrl = "http://localhost:8080/api/internal/bookings/"
                + taskDetail.getBookingId() + "/notify";

        try {
            restTemplate.postForEntity(notifyUrl, new HttpEntity<>(notifyBody, headers), Void.class);
        } catch (Exception e) {
            log.error("Notification delivery failed for taskId={} bookingId={}", taskId, taskDetail.getBookingId(), e);
            return ResponseEntity.status(502).body(Map.of(
                    "error", "Task resolved but notification delivery failed — please retry send"));
        }

        return ResponseEntity.ok(Map.of("status", "sent"));
    }
}
