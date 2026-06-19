package com.planet0088.aiagent.engine.portal.service.implement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planet0088.aiagent.domain.travel.booking.model.Booking;
import com.planet0088.aiagent.domain.travel.booking.model.BookingStatus;
import com.planet0088.aiagent.domain.travel.booking.repository.BookingRepository;
import com.planet0088.aiagent.domain.travel.flight.model.FlightOption;
import com.planet0088.aiagent.domain.travel.flight.model.FlightResearchSubmission;
import com.planet0088.aiagent.engine.portal.dto.PreviewResponse;
import com.planet0088.aiagent.engine.portal.dto.TaskDetailResponse;
import com.planet0088.aiagent.engine.portal.dto.TaskSummaryResponse;
import com.planet0088.aiagent.engine.portal.service.TaskService;
import com.planet0088.aiagent.engine.task.model.ManualTask;
import com.planet0088.aiagent.engine.task.model.ManualTaskStatus;
import com.planet0088.aiagent.engine.task.repository.ManualTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImplement implements TaskService {

    private final ManualTaskRepository manualTaskRepository;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<TaskSummaryResponse> getPendingTasks(String tenantId) {
        List<ManualTask> tasks = manualTaskRepository.findByTenantIdAndStatus(tenantId, ManualTaskStatus.PENDING);

        return tasks.stream()
                .map(task -> {
                    Optional<Booking> bookingOpt = bookingRepository.findByTenantIdAndId(tenantId, task.getBookingId());
                    if (bookingOpt.isEmpty()) {
                        log.warn("Booking not found for taskId={} bookingId={} — skipping", task.getId(), task.getBookingId());
                        return null;
                    }
                    Booking booking = bookingOpt.get();

                    String clientName = (booking.getClientInfo() != null && booking.getClientInfo().getName() != null)
                            ? booking.getClientInfo().getName()
                            : "Unknown";

                    String destination = (booking.getTripDetails() != null)
                            ? booking.getTripDetails().getDestination()
                            : null;

                    return TaskSummaryResponse.builder()
                            .taskId(task.getId())
                            .bookingId(task.getBookingId())
                            .type(task.getType() != null ? task.getType().name() : null)
                            .status(task.getStatus() != null ? task.getStatus().name() : null)
                            .instructions(task.getInstructions())
                            .clientName(clientName)
                            .destination(destination)
                            .createdAt(task.getCreatedAt())
                            .slaDeadlineAt(task.getSlaDeadlineAt())
                            .build();
                })
                .filter(r -> r != null)
                .sorted(Comparator.comparing(TaskSummaryResponse::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public TaskDetailResponse getTaskDetail(String tenantId, String taskId) {
        ManualTask task = manualTaskRepository.findById(taskId)
                .filter(t -> tenantId.equals(t.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, task.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found for task"));

        return TaskDetailResponse.builder()
                .taskId(task.getId())
                .bookingId(task.getBookingId())
                .type(task.getType() != null ? task.getType().name() : null)
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .instructions(task.getInstructions())
                .responseData(task.getResponseData())
                .previewMessage(task.getPreviewMessage())
                .clientInfo(booking.getClientInfo())
                .travelerInfo(booking.getTravelerInfo())
                .tripDetails(booking.getTripDetails())
                .createdAt(task.getCreatedAt())
                .slaDeadlineAt(task.getSlaDeadlineAt())
                .build();
    }

    @Override
    public ManualTask submitResolution(String tenantId, String taskId,
                                      FlightResearchSubmission submission,
                                      String resolvedByStaffId) {
        ManualTask task = manualTaskRepository.findById(taskId)
                .filter(t -> tenantId.equals(t.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() != ManualTaskStatus.PENDING) {
            throw new IllegalStateException("Task is not pending");
        }

        try {
            task.setResponseData(objectMapper.writeValueAsString(submission));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize submission", e);
        }

        task.setStatus(ManualTaskStatus.IN_PROGRESS);
        task.setResolvedBy(resolvedByStaffId);
        task.setUpdatedAt(Instant.now());

        return manualTaskRepository.save(task);
    }

    @Override
    public PreviewResponse generatePreview(String tenantId, String taskId) {
        ManualTask task = manualTaskRepository.findById(taskId)
                .filter(t -> tenantId.equals(t.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getResponseData() == null) {
            throw new IllegalStateException("Must submit options before preview");
        }

        FlightResearchSubmission submission;
        try {
            submission = objectMapper.readValue(task.getResponseData(), FlightResearchSubmission.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response data", e);
        }

        String formattedMessage = formatFlightOptions(submission);

        task.setPreviewMessage(formattedMessage);
        manualTaskRepository.save(task);

        return PreviewResponse.builder()
                .taskId(taskId)
                .formattedMessage(formattedMessage)
                .build();
    }

    private String formatFlightOptions(FlightResearchSubmission submission) {
        StringBuilder sb = new StringBuilder("Great news! We found some flight options for your trip:\n\n");

        List<FlightOption> options = submission.getOptions();
        for (int i = 0; i < options.size(); i++) {
            FlightOption opt = options.get(i);
            sb.append("Option ").append(i + 1).append(": ")
              .append(opt.getAirline()).append(" ").append(opt.getFlightNumber()).append("\n");
            sb.append("Departure: ").append(opt.getDepartureTime())
              .append(" | Arrival: ").append(opt.getArrivalTime()).append("\n");
            sb.append("Price: ").append(opt.getPrice()).append(" ").append(opt.getCurrency())
              .append(" | Stops: ").append(opt.getStops()).append("\n");
            if (opt.getNotes() != null && !opt.getNotes().isBlank()) {
                sb.append(opt.getNotes()).append("\n");
            }
            if (i < options.size() - 1) {
                sb.append("\n");
            }
        }

        if (submission.getStaffNotes() != null && !submission.getStaffNotes().isBlank()) {
            sb.append("\n").append(submission.getStaffNotes()).append("\n");
        }

        sb.append("\nPlease let us know which option works best for you, "
                + "or if you'd like us to look into other alternatives.");

        return sb.toString();
    }

    @Override
    public void confirmSend(String tenantId, String taskId) {
        ManualTask task = manualTaskRepository.findById(taskId)
                .filter(t -> tenantId.equals(t.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getPreviewMessage() == null) {
            throw new IllegalStateException("Must generate preview before sending");
        }
        if (task.getStatus() == ManualTaskStatus.RESOLVED) {
            throw new IllegalStateException("Task already sent");
        }

        task.setStatus(ManualTaskStatus.RESOLVED);
        task.setUpdatedAt(Instant.now());
        manualTaskRepository.save(task);

        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, task.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found for task"));
        booking.setStatus(BookingStatus.CONSULTATION);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);
    }

    @Override
    public String getSessionIdForTask(String tenantId, String taskId) {
        ManualTask task = manualTaskRepository.findById(taskId)
                .filter(t -> tenantId.equals(t.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        return bookingRepository.findByTenantIdAndId(tenantId, task.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"))
                .getSessionId();
    }
}
