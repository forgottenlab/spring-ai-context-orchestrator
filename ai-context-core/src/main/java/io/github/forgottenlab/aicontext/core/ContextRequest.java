package io.github.forgottenlab.aicontext.core;

import java.util.Map;
import java.util.Objects;

/**
 * Input used by context sources to decide what should be loaded for one model turn.
 */
public record ContextRequest(
        String userInput,
        String conversationId,
        Map<String, Object> attributes
) {

    public ContextRequest {
        Objects.requireNonNull(userInput, "userInput must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}