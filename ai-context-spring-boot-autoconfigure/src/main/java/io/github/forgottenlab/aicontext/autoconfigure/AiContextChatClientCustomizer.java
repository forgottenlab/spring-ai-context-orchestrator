package io.github.forgottenlab.aicontext.autoconfigure;

import io.github.forgottenlab.aicontext.springai.ContextOrchestrationAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;

import java.util.Objects;

/**
 * Adds the orchestration advisor to Spring AI's auto-configured builder while
 * preserving Spring AI's builder lifecycle and observability configuration.
 */
public final class AiContextChatClientCustomizer
        implements ChatClientCustomizer {

    private final ContextOrchestrationAdvisor advisor;

    public AiContextChatClientCustomizer(
            ContextOrchestrationAdvisor advisor
    ) {
        this.advisor =
                Objects.requireNonNull(
                        advisor,
                        "advisor must not be null"
                );
    }

    @Override
    public void customize(
            ChatClient.Builder chatClientBuilder
    ) {
        Objects.requireNonNull(
                chatClientBuilder,
                "chatClientBuilder must not be null"
        );

        chatClientBuilder.defaultAdvisors(
                advisor
        );
    }
}