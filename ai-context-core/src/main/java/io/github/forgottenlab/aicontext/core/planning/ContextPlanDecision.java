package io.github.forgottenlab.aicontext.core.planning;

/**
 * Planner decision for one context source.
 */
public enum ContextPlanDecision {
    LOAD,
    DEFER,
    SKIP
}