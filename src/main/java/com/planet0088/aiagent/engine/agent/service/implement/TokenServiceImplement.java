package com.planet0088.aiagent.engine.agent.service.implement;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import com.planet0088.aiagent.engine.agent.model.TokenUsage;
import com.planet0088.aiagent.engine.agent.service.TokenService;
import com.planet0088.aiagent.engine.conversation.model.ConversationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TokenServiceImplement implements TokenService {

    private final EncodingRegistry registry = Encodings.newLazyEncodingRegistry();
    private final Encoding encoding = registry.getEncodingForModel(ModelType.GPT_4O_MINI);

    @Value("${travelagent.agent.history-token-budget}")
    private int historyTokenBudget;

    @Value("${travelagent.agent.pricing.prompt-token-cost}")
    private double promptTokenCost;

    @Value("${travelagent.agent.pricing.completion-token-cost}")
    private double completionTokenCost;

    @Override
    public int countTokens(String text) {
        return encoding.countTokens(text);
    }

    @Override
    public int countTokens(List<ConversationMessage> messages) {
        int total = 0;
        for (ConversationMessage msg : messages) {
            total += countTokens(msg.getContent());
            total += 4; // per-message role overhead (OpenAI chat format)
        }
        return total;
    }

    @Override
    public List<ConversationMessage> trimHistoryToTokenBudget(List<ConversationMessage> history) {
        if (history.size() <= 2) return new ArrayList<>(history);

        List<ConversationMessage> kept = new ArrayList<>();
        int tokens = 0;

        for (int i = history.size() - 1; i >= 0; i--) {
            ConversationMessage msg = history.get(i);
            int msgTokens = countTokens(msg.getContent()) + 4;
            boolean mustKeep = kept.size() < 2; // always keep at least the last 2

            if (mustKeep || tokens + msgTokens <= historyTokenBudget) {
                kept.add(0, msg);
                tokens += msgTokens;
            } else {
                break;
            }
        }

        log.debug("History trimmed to {} messages, {} tokens", kept.size(), tokens);
        return kept;
    }

    @Override
    public TokenUsage buildUsageRecord(String model, int promptTokens, int completionTokens) {
        double cost = promptTokens * promptTokenCost + completionTokens * completionTokenCost;
        return TokenUsage.builder()
                .model(model)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(promptTokens + completionTokens)
                .estimatedCostUsd(cost)
                .createdAt(Instant.now())
                .build();
    }
}
