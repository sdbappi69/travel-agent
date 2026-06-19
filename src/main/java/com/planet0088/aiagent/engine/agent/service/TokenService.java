package com.planet0088.aiagent.engine.agent.service;

import com.planet0088.aiagent.engine.agent.model.TokenUsage;
import com.planet0088.aiagent.engine.conversation.model.ConversationMessage;

import java.util.List;

public interface TokenService {

    int countTokens(String text);

    int countTokens(List<ConversationMessage> messages);

    List<ConversationMessage> trimHistoryToTokenBudget(List<ConversationMessage> history);

    TokenUsage buildUsageRecord(String model, int promptTokens, int completionTokens);
}
