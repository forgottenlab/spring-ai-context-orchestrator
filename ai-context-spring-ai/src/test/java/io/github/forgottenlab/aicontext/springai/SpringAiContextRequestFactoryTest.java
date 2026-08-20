package io.github.forgottenlab.aicontext.springai;

import io.github.forgottenlab.aicontext.core.ContextRequest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpringAiContextRequestFactoryTest {

    private final SpringAiContextRequestFactory factory =
            new SpringAiContextRequestFactory();

    @Test
    void extractsUserInput() {
        ContextRequest request =
                factory.create(chatRequest("recommend a keyboard", Map.of()));

        assertEquals(
                "recommend a keyboard",
                request.userInput()
        );
    }

    @Test
    void extractsExplicitConversationIdAndRequestAttributes() {
        Map<String, Object> context =
                new LinkedHashMap<>();

        context.put(
                SpringAiContextAttributes.CONVERSATION_ID,
                "conversation-42"
        );

        context.put(
                SpringAiContextAttributes.REQUEST_ATTRIBUTES,
                Map.of(
                        "userId", 42L,
                        "tenant", "demo"
                )
        );

        ContextRequest request =
                factory.create(
                        chatRequest("hello", context)
                );

        assertEquals(
                "conversation-42",
                request.conversationId()
        );
        assertEquals(42L, request.attributes().get("userId"));
        assertEquals("demo", request.attributes().get("tenant"));
    }

    @Test
    void doesNotLeakUnrelatedAdvisorContextIntoCoreAttributes() {
        Map<String, Object> context =
                Map.of(
                        "secret-internal-advisor-value",
                        "must-not-leak"
                );

        ContextRequest request =
                factory.create(
                        chatRequest("hello", context)
                );

        assertTrue(request.attributes().isEmpty());
    }

    @Test
    void missingBridgeAttributesProduceSafeDefaults() {
        ContextRequest request =
                factory.create(
                        chatRequest("hello", Map.of())
                );

        assertNull(request.conversationId());
        assertTrue(request.attributes().isEmpty());
    }

    @Test
    void rejectsInvalidBridgeAttributeTypes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> factory.create(
                        chatRequest(
                                "hello",
                                Map.of(
                                        SpringAiContextAttributes.CONVERSATION_ID,
                                        42
                                )
                        )
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> factory.create(
                        chatRequest(
                                "hello",
                                Map.of(
                                        SpringAiContextAttributes.REQUEST_ATTRIBUTES,
                                        "not-a-map"
                                )
                        )
                )
        );
    }

    private static ChatClientRequest chatRequest(
            String userText,
            Map<String, Object> context
    ) {
        return new ChatClientRequest(
                new Prompt(
                        new UserMessage(userText)
                ),
                context
        );
    }
}