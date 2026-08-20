package io.github.forgottenlab.aicontext.core;

import java.time.Duration;
import java.util.Objects;

/**
 * Execution constraints for one context source.
 *
 * <p>The timeout is a logical orchestration timeout. A timed-out provider may
 * still need provider-specific cancellation if its underlying client cannot be
 * interrupted by CompletableFuture cancellation.</p>
 */
public record ContextExecutionPolicy(Duration timeout) {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(3);

    public ContextExecutionPolicy {
        Objects.requireNonNull(timeout, "timeout must not be null");

        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be greater than zero");
        }
    }

    public static ContextExecutionPolicy defaults() {
        return new ContextExecutionPolicy(DEFAULT_TIMEOUT);
    }
}