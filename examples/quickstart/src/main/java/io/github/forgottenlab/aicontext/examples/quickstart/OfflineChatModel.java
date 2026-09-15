package io.github.forgottenlab.aicontext.examples.quickstart;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
final class OfflineChatModel implements ChatModel {

    static final String RESPONSE = "offline-response";

    private final AtomicReference<Prompt> lastPrompt =
            new AtomicReference<>();

    private final AtomicInteger callCount =
            new AtomicInteger();

    @Override
    public ChatResponse call(Prompt prompt) {
        lastPrompt.set(prompt);
        callCount.incrementAndGet();

        return new ChatResponse(
                List.of(
                        new Generation(
                                new AssistantMessage(RESPONSE)
                        )
                )
        );
    }

    Prompt lastPrompt() {
        return lastPrompt.get();
    }

    int callCount() {
        return callCount.get();
    }
}
