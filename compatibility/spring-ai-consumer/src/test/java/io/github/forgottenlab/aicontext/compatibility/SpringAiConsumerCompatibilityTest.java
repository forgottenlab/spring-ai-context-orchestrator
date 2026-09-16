package io.github.forgottenlab.aicontext.compatibility;

import io.github.forgottenlab.aicontext.core.ContextAuthority;
import io.github.forgottenlab.aicontext.core.ContextContribution;
import io.github.forgottenlab.aicontext.core.ContextItem;
import io.github.forgottenlab.aicontext.core.ContextKey;
import io.github.forgottenlab.aicontext.core.ContextPriority;
import io.github.forgottenlab.aicontext.core.ContextRequest;
import io.github.forgottenlab.aicontext.core.ContextSource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = SpringAiConsumerCompatibilityTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class SpringAiConsumerCompatibilityTest {

    private static final String SYSTEM_MESSAGE =
            "You are the compatibility assistant.";

    private static final String USER_MESSAGE =
            "Is the consumer path active?";

    @Autowired
    private ConsumerAssistant assistant;

    @Autowired
    private CapturingChatModel chatModel;

    @Test
    void enrichesPromptThroughExternalStarterConsumerPath() {
        assertThat(assistant.ask()).isEqualTo("compatibility-response");
        assertThat(chatModel.callCount()).isEqualTo(1);

        Prompt prompt = chatModel.lastPrompt();
        assertThat(prompt).isNotNull();
        assertThat(prompt.getUserMessage().getText())
                .isEqualTo(USER_MESSAGE);

        String systemText = prompt.getSystemMessage().getText();
        assertThat(systemText)
                .contains(SYSTEM_MESSAGE)
                .contains("<business-context>")
                .contains("</business-context>")
                .contains("source=\"compatibility\"")
                .contains("compatibility=verified")
                .containsOnlyOnce("<business-context>");
        assertThat(systemText.indexOf(SYSTEM_MESSAGE))
                .isLessThan(systemText.indexOf("<business-context>"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {

        @Bean
        CapturingChatModel chatModel() {
            return new CapturingChatModel();
        }

        @Bean
        ContextSource compatibilityContextSource() {
            return new ContextSource() {
                @Override
                public String id() {
                    return "compatibility";
                }

                @Override
                public CompletableFuture<ContextContribution> load(
                        ContextRequest request
                ) {
                    ContextItem<String> item = new ContextItem<>(
                            "compatibility-status",
                            "compatibility=verified",
                            ContextKey.of(
                                    "compatibility",
                                    "spring-ai",
                                    "consumer-path"
                            ),
                            ContextPriority.REQUIRED,
                            ContextAuthority.AUTHORITATIVE,
                            Instant.parse("2026-09-15T00:00:00Z"),
                            null,
                            Map.of()
                    );

                    return CompletableFuture.completedFuture(
                            new ContextContribution(
                                    id(),
                                    List.of(item)
                            )
                    );
                }
            };
        }

        @Bean
        ConsumerAssistant consumerAssistant(
                ChatClient.Builder builder
        ) {
            return new ConsumerAssistant(builder);
        }
    }

    static final class ConsumerAssistant {

        private final ChatClient chatClient;

        ConsumerAssistant(ChatClient.Builder builder) {
            this.chatClient = builder.build();
        }

        String ask() {
            return chatClient.prompt()
                    .system(SYSTEM_MESSAGE)
                    .user(USER_MESSAGE)
                    .call()
                    .content();
        }
    }

    static final class CapturingChatModel implements ChatModel {

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
                                    new AssistantMessage(
                                            "compatibility-response"
                                    )
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
}
