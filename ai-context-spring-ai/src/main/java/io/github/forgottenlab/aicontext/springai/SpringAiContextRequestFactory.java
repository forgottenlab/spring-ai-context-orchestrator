package io.github.forgottenlab.aicontext.springai;

import io.github.forgottenlab.aicontext.core.ContextRequest;
import org.springframework.ai.chat.client.ChatClientRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Converts a Spring AI ChatClientRequest into the provider-neutral core
 * ContextRequest.
 */
public final class SpringAiContextRequestFactory {

    public ContextRequest create(ChatClientRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String userInput =
                Objects.toString(
                        request.prompt().getUserMessage().getText(),
                        ""
                );

        String conversationId =
                optionalString(
                        request.context().get(
                                SpringAiContextAttributes.CONVERSATION_ID
                        ),
                        SpringAiContextAttributes.CONVERSATION_ID
                );

        Map<String, Object> attributes =
                requestAttributes(
                        request.context().get(
                                SpringAiContextAttributes.REQUEST_ATTRIBUTES
                        )
                );

        return new ContextRequest(
                userInput,
                conversationId,
                attributes
        );
    }

    private static String optionalString(
            Object value,
            String attributeName
    ) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof String stringValue)) {
            throw new IllegalArgumentException(
                    attributeName + " must be a String"
            );
        }

        return stringValue;
    }

    private static Map<String, Object> requestAttributes(
            Object value
    ) {
        if (value == null) {
            return Map.of();
        }

        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException(
                    SpringAiContextAttributes.REQUEST_ATTRIBUTES
                            + " must be a Map<String, Object>"
            );
        }

        Map<String, Object> result =
                new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException(
                        SpringAiContextAttributes.REQUEST_ATTRIBUTES
                                + " keys must be Strings"
                );
            }

            Object attributeValue =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "request attribute value must not be null: " + key
                    );

            result.put(key, attributeValue);
        }

        return Map.copyOf(result);
    }
}