package io.github.forgottenlab.aicontext.core.execution;

import java.time.Duration;
import java.util.Objects;

/**
 * Failed or timed-out execution of one context source.
 */
public record ContextExecutionFailure(
        String sourceId,
        ContextExecutionFailureKind kind,
        Throwable cause,
        Duration elapsed
) implements ContextExecutionResult {

    public ContextExecutionFailure {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(kind, "kind must not be null");
        Objects.requireNonNull(cause, "cause must not be null");
        Objects.requireNonNull(elapsed, "elapsed must not be null");
    }
}