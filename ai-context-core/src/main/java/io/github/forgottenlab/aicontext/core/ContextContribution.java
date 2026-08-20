package io.github.forgottenlab.aicontext.core;

import java.util.List;
import java.util.Objects;

/**
 * A source-scoped batch of context items.
 */
public record ContextContribution(
        String sourceId,
        List<ContextItem<?>> items
) {

    public ContextContribution {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static ContextContribution empty(String sourceId) {
        return new ContextContribution(sourceId, List.of());
    }
}