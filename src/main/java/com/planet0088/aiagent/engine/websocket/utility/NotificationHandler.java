package com.planet0088.aiagent.engine.websocket.utility;

import com.planet0088.aiagent.engine.websocket.model.NotificationMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationHandler implements WebSocketHandler {

    private final SessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = extractSessionId(session);
        if (sessionId == null) {
            log.warn("WebSocket connection without sessionId — closing");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        sessionRegistry.register(sessionId, session);

        Query query = Query.query(Criteria.where("sessionId").is(sessionId));
        List<Document> missed = mongoTemplate.find(query, Document.class, "missed_notifications");
        for (Document doc : missed) {
            String messageJson = doc.getString("message");
            if (messageJson != null) {
                session.sendMessage(new TextMessage(messageJson));
            }
        }
        if (!missed.isEmpty()) {
            mongoTemplate.remove(query, "missed_notifications");
        }

        log.info("WebSocket connected: {}", sessionId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (!(message instanceof TextMessage textMessage)) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(textMessage.getPayload());
            if ("PING".equals(node.path("type").asText())) {
                session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
            }
        } catch (Exception e) {
            log.warn("Failed to parse WebSocket message from session {}", extractSessionId(session), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String sessionId = extractSessionId(session);
        log.error("WebSocket transport error for session {}", sessionId, exception);
        if (sessionId != null) {
            sessionRegistry.unregister(sessionId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = extractSessionId(session);
        if (sessionId != null) {
            sessionRegistry.unregister(sessionId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void sendToSession(String sessionId, NotificationMessage message) {
        sessionRegistry.getSession(sessionId).filter(WebSocketSession::isOpen).ifPresentOrElse(
                session -> {
                    try {
                        String json = objectMapper.writeValueAsString(message);
                        session.sendMessage(new TextMessage(json));
                    } catch (Exception e) {
                        log.error("Failed to send WebSocket message to {}", sessionId, e);
                        storeMissed(sessionId, message);
                    }
                },
                () -> {
                    log.warn("Session {} not connected — message stored for replay", sessionId);
                    storeMissed(sessionId, message);
                }
        );
    }

    private void storeMissed(String sessionId, NotificationMessage message) {
        try {
            Document doc = new Document();
            doc.put("sessionId", sessionId);
            doc.put("message", objectMapper.writeValueAsString(message));
            doc.put("createdAt", Instant.now().toString());
            mongoTemplate.insert(doc, "missed_notifications");
        } catch (Exception e) {
            log.error("Failed to store missed notification for session {}", sessionId, e);
        }
    }

    private String extractSessionId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2 && "sessionId".equals(parts[0])) {
                return parts[1];
            }
        }
        return null;
    }
}
