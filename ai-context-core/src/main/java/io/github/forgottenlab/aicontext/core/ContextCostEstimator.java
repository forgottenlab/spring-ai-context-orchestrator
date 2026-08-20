package io.github.forgottenlab.aicontext.core;

/**
 * Estimates how much model-context budget one candidate consumes.
 *
 * <p>Implementations must return a non-negative value and should be
 * deterministic for the same candidate whenever possible.</p>
 */
@FunctionalInterface
public interface ContextCostEstimator {

    long estimate(ContextCandidate candidate);
}