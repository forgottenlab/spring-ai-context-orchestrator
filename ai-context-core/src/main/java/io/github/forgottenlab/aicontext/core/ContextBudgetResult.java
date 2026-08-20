package io.github.forgottenlab.aicontext.core;

import java.util.List;

/**
 * Immutable result of applying the model-context budget.
 */
public record ContextBudgetResult(
        List<ContextCandidate> selected,
        List<ContextBudgetRejection> rejected,
        long usedCost,
        long maxCost
) {

    public ContextBudgetResult {
        selected = selected == null ? List.of() : List.copyOf(selected);
        rejected = rejected == null ? List.of() : List.copyOf(rejected);

        if (usedCost < 0) {
            throw new IllegalArgumentException("usedCost must not be negative");
        }

        if (maxCost <= 0) {
            throw new IllegalArgumentException("maxCost must be greater than zero");
        }

        if (usedCost > maxCost) {
            throw new IllegalArgumentException(
                    "usedCost must not exceed maxCost"
            );
        }
    }

    public long remainingCost() {
        return maxCost - usedCost;
    }

    public boolean hasRejections() {
        return !rejected.isEmpty();
    }
}