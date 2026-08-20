package io.github.forgottenlab.aicontext.core;

import java.util.Objects;

/**
 * Diagnostic record for a candidate omitted by the budget stage.
 */
public record ContextBudgetRejection(
        ContextCandidate candidate,
        long estimatedCost,
        ContextBudgetRejectionReason reason
) {

    public ContextBudgetRejection {
        Objects.requireNonNull(candidate, "candidate must not be null");
        Objects.requireNonNull(reason, "reason must not be null");

        if (estimatedCost < 0) {
            throw new IllegalArgumentException(
                    "estimatedCost must not be negative"
            );
        }
    }
}