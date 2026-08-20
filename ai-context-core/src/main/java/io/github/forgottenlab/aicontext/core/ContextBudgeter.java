package io.github.forgottenlab.aicontext.core;

/**
 * Applies a model-context budget after context resolution.
 */
public interface ContextBudgeter {

    ContextBudgetResult apply(
            ContextResolution resolution,
            ContextBudgetPolicy policy,
            ContextCostEstimator estimator
    );
}