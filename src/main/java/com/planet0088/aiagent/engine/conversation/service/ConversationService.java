package com.planet0088.aiagent.engine.conversation.service;

import com.planet0088.aiagent.engine.conversation.model.ConversationMessage;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

public interface ConversationService {

    void addMessage(String tenantId, String bookingId, ConversationMessage message);

    List<ConversationMessage> getHistory(String tenantId, String bookingId);

    List<Message> buildOpenAiMessages(List<ConversationMessage> history);

    String injectAgentName(String systemPrompt, String agentName);

    void clearCache(String tenantId, String bookingId);
}
