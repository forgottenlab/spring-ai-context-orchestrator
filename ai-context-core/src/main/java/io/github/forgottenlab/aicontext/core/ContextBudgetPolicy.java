package io.github.forgottenlab.aicontext.core;

/**
 * Model-context budget expressed in estimator-defined cost units.
 *
 * <p>The core intentionally does not prescribe a tokenizer. An integration
 * layer may interpret one cost unit as one model token, while a lightweight
 * application may use a deterministic approximation.</p>
 */
public record ContextBudgetPolicy(long maxCost) {

    public ContextBudgetPolicy {
        if (maxCost <= 0) {
            throw new IllegalArgumentException("maxCost must be greater than zero");
        }
    }
}