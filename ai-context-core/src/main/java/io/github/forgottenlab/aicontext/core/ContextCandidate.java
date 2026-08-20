package io.github.forgottenlab.aicontext.core;

import java.util.Objects;

/**
 * Context item paired with the source that produced it.
 */
public record ContextCandidate(
        String sourceId,
        ContextItem<?> item
) {

    public ContextCandidate {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(item, "item must not be null");
    }
}