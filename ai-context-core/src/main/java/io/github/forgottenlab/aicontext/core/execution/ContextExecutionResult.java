package io.github.forgottenlab.aicontext.core.execution;

import java.time.Duration;

/**
 * Result of executing one planned context source.
 */
public sealed interface ContextExecutionResult
        permits ContextExecutionSuccess, ContextExecutionFailure {

    String sourceId();

    Duration elapsed();

    default boolean isSuccess() {
        return this instanceof ContextExecutionSuccess;
    }
}