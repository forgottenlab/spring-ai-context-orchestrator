package io.github.forgottenlab.aicontext.core.planning;

import io.github.forgottenlab.aicontext.core.ContextLoadStrategy;
import java.util.Objects;

/**
 * Immutable planning result for one context source.
 */
public record ContextPlanEntry(
        String sourceId,
        ContextLoadStrategy strategy,
        ContextPlanDecision decision
) {

    public ContextPlanEntry {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(strategy, "strategy must not be null");
        Objects.requireNonNull(decision, "decision must not be null");
    }
}