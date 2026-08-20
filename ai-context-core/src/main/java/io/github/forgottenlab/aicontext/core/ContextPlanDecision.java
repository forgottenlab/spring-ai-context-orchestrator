package io.github.forgottenlab.aicontext.core;

/**
 * Planner decision for one context source.
 */
public enum ContextPlanDecision {
    LOAD,
    DEFER,
    SKIP
}