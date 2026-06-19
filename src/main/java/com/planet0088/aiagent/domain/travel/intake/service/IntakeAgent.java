package com.planet0088.aiagent.domain.travel.intake.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planet0088.aiagent.domain.travel.booking.model.ClientInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TravelerInfo;
import com.planet0088.aiagent.domain.travel.booking.model.TripDetails;
import com.planet0088.aiagent.domain.travel.booking.service.BookingService;
import com.planet0088.aiagent.engine.conversation.model.ConversationMessage;
import com.planet0088.aiagent.engine.conversation.model.MessageRole;
import com.planet0088.aiagent.engine.conversation.service.ConversationService;
import com.planet0088.aiagent.engine.task.service.ManualTaskService;
import com.planet0088.aiagent.engine.tenant.model.Tenant;
import com.planet0088.aiagent.engine.tenant.service.TenantService;
import com.planet0088.aiagent.engine.websocket.utility.NotificationHandler;
import com.planet0088.aiagent.engine.websocket.model.NotificationMessage;
import com.planet0088.aiagent.engine.agent.service.TokenService;
import com.planet0088.aiagent.engine.agent.model.TokenUsage;
import com.planet0088.aiagent.engine.agent.repository.TokenUsageRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IntakeAgent {

    private final ChatModel chatModel;
    private final ConversationService conversationService;
    private final BookingService bookingService;
    private final NotificationHandler notificationHandler;
    private final ObjectMapper objectMapper;
    private final ManualTaskService manualTaskService;
    private final TokenService tokenService;
    private final TokenUsageRepository tokenUsageRepository;
    private final TenantService tenantService;
    private final Executor taskExecutor;

    @Value("${travelagent.agent.intake.system-prompt}")
    private Resource systemPromptResource;

    @Value("${travelagent.agent.extraction.prompt}")
    private Resource extractionPromptResource;

    private String systemPrompt;
    private String extractionPrompt;

    public IntakeAgent(ChatModel chatModel,
                       ConversationService conversationService,
                       BookingService bookingService,
                       NotificationHandler notificationHandler,
                       ObjectMapper objectMapper,
                       ManualTaskService manualTaskService,
                       TokenService tokenService,
                       TokenUsageRepository tokenUsageRepository,
                       TenantService tenantService,
                       @Qualifier("agentTaskExecutor") Executor taskExecutor) {
        this.chatModel = chatModel;
        this.conversationService = conversationService;
        this.bookingService = bookingService;
        this.notificationHandler = notificationHandler;
        this.objectMapper = objectMapper;
        this.manualTaskService = manualTaskService;
        this.tokenService = tokenService;
        this.tokenUsageRepository = tokenUsageRepository;
        this.tenantService = tenantService;
        this.taskExecutor = taskExecutor;
    }

    @PostConstruct
    public void init() throws IOException {
        systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
        extractionPrompt = extractionPromptResource.getContentAsString(StandardCharsets.UTF_8);
    }

    public void sendWelcomeMessage(String tenantId, String bookingId, String sessionId) {
        try {
            Tenant tenant = tenantService.getByTenantId(tenantId);
            String agentName = (tenant.getSettings() != null
                    && tenant.getSettings().getAgentName() != null
                    && !tenant.getSettings().getAgentName().isBlank())
                    ? tenant.getSettings().getAgentName()
                    : "Your Travel Consultant";

            String resolvedPrompt = buildDateContext() + conversationService.injectAgentName(systemPrompt, agentName);

            List<Message> messages = List.of(
                    new SystemMessage(resolvedPrompt),
                    new UserMessage("BEGIN_CONVERSATION")
            );

            ChatResponse response = chatModel.call(new Prompt(messages));
            String welcomeMessage = response.getResult().getOutput().getText().trim();

            conversationService.addMessage(tenantId, bookingId, ConversationMessage.builder()
                    .role(MessageRole.ASSISTANT.name())
                    .content(welcomeMessage)
                    .timestamp(Instant.now())
                    .build());

            int promptTokens = tokenService.countTokens(resolvedPrompt)
                             + tokenService.countTokens("BEGIN_CONVERSATION");
            int completionTokens = tokenService.countTokens(welcomeMessage);
            tokenUsageRepository.save(TokenUsage.builder()
                    .tenantId(tenantId)
                    .bookingId(bookingId)
                    .conversationId(bookingId)
                    .model("gpt-4o-mini")
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(promptTokens + completionTokens)
                    .estimatedCostUsd(promptTokens * 0.00000015 + completionTokens * 0.0000006)
                    .createdAt(Instant.now())
                    .build());

            notificationHandler.sendToSession(sessionId, NotificationMessage.builder()
                    .type("AGENT_MESSAGE")
                    .message(welcomeMessage)
                    .timestamp(Instant.now())
                    .build());

            log.info("Welcome message sent for bookingId: {}", bookingId);

        } catch (Exception e) {
            log.error("Failed to send welcome message for bookingId: {}", bookingId, e);
            notificationHandler.sendToSession(sessionId, NotificationMessage.builder()
                    .type("AGENT_MESSAGE")
                    .message("Hello! I'm your travel consultant. What's your name?")
                    .timestamp(Instant.now())
                    .build());
        }
    }

    public void streamResponse(String tenantId, String bookingId, String sessionId,
                               String userMessage, SseEmitter emitter) {

        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> log.error("SSE error for booking {}", bookingId, e));

        Tenant tenant = tenantService.getByTenantId(tenantId);
        String agentName = tenant.getSettings() != null ? tenant.getSettings().getAgentName() : null;
        String resolvedPrompt = buildDateContext() + conversationService.injectAgentName(systemPrompt, agentName);

        ConversationMessage userMsg = ConversationMessage.builder()
                .role("USER")
                .content(userMessage)
                .timestamp(Instant.now())
                .build();
        conversationService.addMessage(tenantId, bookingId, userMsg);

        List<ConversationMessage> history = conversationService.getHistory(tenantId, bookingId);
        List<ConversationMessage> trimmedHistory = tokenService.trimHistoryToTokenBudget(history);
        log.debug("Sending {} messages to OpenAI after token trimming", trimmedHistory.size());

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(resolvedPrompt));
        messages.addAll(conversationService.buildOpenAiMessages(trimmedHistory));

        int promptTokens = tokenService.countTokens(trimmedHistory)
                         + tokenService.countTokens(resolvedPrompt)
                         + tokenService.countTokens(userMessage);

        StringBuilder fullResponse = new StringBuilder();

        chatModel.stream(new Prompt(messages)).subscribe(
                response -> {
                    try {
                        String chunk = response.getResult().getOutput().getText();
                        if (chunk != null && !chunk.isEmpty()) {
                            emitter.send(SseEmitter.event().data(chunk));
                            fullResponse.append(chunk);
                        }
                    } catch (IOException e) {
                        log.error("SSE send error for booking {}", bookingId, e);
                    }
                },
                error -> {
                    log.error("Streaming error for booking {}", bookingId, error);
                    emitter.completeWithError(error);
                },
                () -> {
                    ConversationMessage assistantMsg = ConversationMessage.builder()
                            .role("ASSISTANT")
                            .content(fullResponse.toString())
                            .timestamp(Instant.now())
                            .build();
                    conversationService.addMessage(tenantId, bookingId, assistantMsg);

                    int completionTokens = tokenService.countTokens(fullResponse.toString());
                    int totalTokens = promptTokens + completionTokens;
                    double cost = promptTokens * 0.00000015 + completionTokens * 0.0000006;
                    TokenUsage usage = TokenUsage.builder()
                            .tenantId(tenantId)
                            .bookingId(bookingId)
                            .conversationId(bookingId)
                            .model("gpt-4o-mini")
                            .promptTokens(promptTokens)
                            .completionTokens(completionTokens)
                            .totalTokens(totalTokens)
                            .estimatedCostUsd(cost)
                            .createdAt(Instant.now())
                            .build();
                    tokenUsageRepository.save(usage);
                    log.debug("Token usage saved: {} prompt, {} completion, ${} estimated",
                            promptTokens, completionTokens, cost);

                    String lower = fullResponse.toString().toLowerCase(Locale.ROOT);
                    boolean isConfirmation = lower.contains("checking availability")
                            || lower.contains("we have everything we need")
                            || lower.contains("will get back to you")
                            || lower.contains("checking for you");

                    if (isConfirmation) {
                        taskExecutor.execute(() -> extractBookingData(tenantId, bookingId, sessionId));
                    }

                    try {
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("Failed to complete SSE for booking {}", bookingId, e);
                    }
                }
        );
    }

    private String buildDateContext() {
        return "Today's date is "
                + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                + ". Use this as the reference point for any relative or year-less dates "
                + "the client mentions (e.g. 'next Friday', 'July 24th' without a year). "
                + "Always assume future dates unless explicitly stated otherwise. "
                + "If the trip spans a year boundary (for example, a departure in December "
                + "and a return in January), assume the return date falls in the following "
                + "calendar year, since a return date must always be chronologically after "
                + "the departure date.\n\n";
    }

    private void extractBookingData(String tenantId, String bookingId, String sessionId) {
        try {
            List<ConversationMessage> history = conversationService.getHistory(tenantId, bookingId);

            String transcript = history.stream()
                    .map(m -> m.getRole() + ": " + m.getContent())
                    .collect(Collectors.joining("\n"));

            String resolvedExtractionPrompt = buildDateContext() + extractionPrompt;

            List<Message> extractionMessages = List.of(
                    new SystemMessage(resolvedExtractionPrompt),
                    new UserMessage("Here is the conversation transcript:\n\n" + transcript)
            );

            ChatResponse response = chatModel.call(new Prompt(extractionMessages));
            String json = response.getResult().getOutput().getText().trim();

            int promptTokens = tokenService.countTokens(resolvedExtractionPrompt)
                             + tokenService.countTokens(transcript);
            int completionTokens = tokenService.countTokens(json);
            TokenUsage usage = TokenUsage.builder()
                    .tenantId(tenantId)
                    .bookingId(bookingId)
                    .conversationId(bookingId)
                    .model("gpt-4o-mini")
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(promptTokens + completionTokens)
                    .estimatedCostUsd(promptTokens * 0.00000015 + completionTokens * 0.0000006)
                    .createdAt(Instant.now())
                    .build();
            tokenUsageRepository.save(usage);

            if (json.startsWith("```")) {
                json = json.replaceAll("```json", "").replaceAll("```", "").trim();
            }

            JsonNode root = objectMapper.readTree(json);
            ClientInfo clientInfo = objectMapper.treeToValue(root.get("clientInfo"), ClientInfo.class);
            TravelerInfo travelerInfo = objectMapper.treeToValue(root.get("travelerInfo"), TravelerInfo.class);
            TripDetails tripDetails = objectMapper.treeToValue(root.get("tripDetails"), TripDetails.class);

            bookingService.finalizeIntake(tenantId, bookingId, clientInfo, travelerInfo, tripDetails);

            notificationHandler.sendToSession(sessionId, NotificationMessage.builder()
                    .type("BOOKING_READY")
                    .message("We have received your details. We are now checking availability and will get back to you shortly.")
                    .timestamp(Instant.now())
                    .build());

            log.info("Booking extraction successful for bookingId: {}", bookingId);

        } catch (Exception e) {
            log.error("Extraction failed for bookingId: {}", bookingId, e);
            manualTaskService.createEscalationTask(tenantId, bookingId,
                    "Automated extraction failed after client confirmation. Manual data entry required.");
            notificationHandler.sendToSession(sessionId, NotificationMessage.builder()
                    .type("ERROR")
                    .message("Sorry, something went wrong. An agent will assist you shortly.")
                    .timestamp(Instant.now())
                    .build());
        }
    }
}
