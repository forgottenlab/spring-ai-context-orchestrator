package io.github.forgottenlab.aicontext.core;

import java.time.Duration;
import java.util.Objects;

/**
 * Successful execution of one context source.
 */
public record ContextExecutionSuccess(
        String sourceId,
        ContextContribution contribution,
        Duration elapsed
) implements ContextExecutionResult {

    public ContextExecutionSuccess {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(contribution, "contribution must not be null");
        Objects.requireNonNull(elapsed, "elapsed must not be null");
    }
}