package com.planet0088.aiagent.engine.conversation.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planet0088.aiagent.engine.conversation.model.ConversationMessage;
import com.planet0088.aiagent.engine.conversation.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImplement implements ConversationService {

    private static final Duration TTL = Duration.ofHours(2);

    private final StringRedisTemplate redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    private String redisKey(String tenantId, String bookingId) {
        return "conversation:" + tenantId + ":" + bookingId;
    }

    @Override
    public void addMessage(String tenantId, String bookingId, ConversationMessage message) {
        String key = redisKey(tenantId, bookingId);
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key, json);
            redisTemplate.expire(key, TTL);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for key {}", key, e);
        }

        Query query = Query.query(Criteria.where("tenantId").is(tenantId).and("_id").is(bookingId));
        Update update = new Update().push("messages", message);
        mongoTemplate.updateFirst(query, update, "bookings");
    }

    @Override
    public List<ConversationMessage> getHistory(String tenantId, String bookingId) {
        String key = redisKey(tenantId, bookingId);
        List<String> entries = redisTemplate.opsForList().range(key, 0, -1);

        if (entries != null && !entries.isEmpty()) {
            List<ConversationMessage> messages = new ArrayList<>();
            for (String entry : entries) {
                try {
                    messages.add(objectMapper.readValue(entry, ConversationMessage.class));
                } catch (JsonProcessingException e) {
                    log.warn("Failed to deserialize message from Redis key {}", key, e);
                }
            }
            return messages;
        }

        Query query = Query.query(Criteria.where("tenantId").is(tenantId).and("_id").is(bookingId));
        query.fields().include("messages");
        com.planet0088.aiagent.domain.travel.booking.model.Booking booking =
                mongoTemplate.findOne(query, com.planet0088.aiagent.domain.travel.booking.model.Booking.class, "bookings");

        List<ConversationMessage> messages = (booking != null && booking.getMessages() != null)
                ? booking.getMessages()
                : new ArrayList<>();

        if (!messages.isEmpty()) {
            for (ConversationMessage msg : messages) {
                try {
                    String json = objectMapper.writeValueAsString(msg);
                    redisTemplate.opsForList().rightPush(key, json);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to repopulate Redis for key {}", key, e);
                }
            }
            redisTemplate.expire(key, TTL);
        }

        return messages;
    }

    @Override
    public List<Message> buildOpenAiMessages(List<ConversationMessage> history) {
        List<Message> messages = new ArrayList<>();
        for (ConversationMessage msg : history) {
            messages.add(switch (msg.getRole().toUpperCase()) {
                case "ASSISTANT" -> new AssistantMessage(msg.getContent());
                case "SYSTEM"    -> new SystemMessage(msg.getContent());
                default          -> new UserMessage(msg.getContent());
            });
        }
        return messages;
    }

    @Override
    public String injectAgentName(String systemPrompt, String agentName) {
        String name = (agentName != null && !agentName.isBlank())
                ? agentName
                : "Your Travel Consultant";
        return systemPrompt.replace("{agentName}", name);
    }

    @Override
    public void clearCache(String tenantId, String bookingId) {
        redisTemplate.delete(redisKey(tenantId, bookingId));
    }
}
