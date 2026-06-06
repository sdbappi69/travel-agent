package com.planet0088.travelAgent.agent;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import com.planet0088.travelAgent.conversation.ConversationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TokenService {

    private final EncodingRegistry registry = Encodings.newLazyEncodingRegistry();
    private final Encoding encoding = registry.getEncodingForModel(ModelType.GPT_4O_MINI);

    private static final int HISTORY_TOKEN_BUDGET = 2000;

    public int countTokens(String text) {
        return encoding.countTokens(text);
    }

    public int countTokens(List<ConversationMessage> messages) {
        int total = 0;
        for (ConversationMessage msg : messages) {
            total += countTokens(msg.getContent());
            total += 4; // per-message role overhead (OpenAI chat format)
        }
        return total;
    }

    public List<ConversationMessage> trimHistoryToTokenBudget(List<ConversationMessage> history) {
        if (history.size() <= 2) return new ArrayList<>(history);

        List<ConversationMessage> kept = new ArrayList<>();
        int tokens = 0;

        for (int i = history.size() - 1; i >= 0; i--) {
            ConversationMessage msg = history.get(i);
            int msgTokens = countTokens(msg.getContent()) + 4;
            boolean mustKeep = kept.size() < 2; // always keep at least the last 2

            if (mustKeep || tokens + msgTokens <= HISTORY_TOKEN_BUDGET) {
                kept.add(0, msg);
                tokens += msgTokens;
            } else {
                break;
            }
        }

        log.debug("History trimmed to {} messages, {} tokens", kept.size(), tokens);
        return kept;
    }

    public TokenUsage buildUsageRecord(String model, int promptTokens, int completionTokens) {
        double cost = promptTokens * 0.00000015 + completionTokens * 0.0000006;
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
