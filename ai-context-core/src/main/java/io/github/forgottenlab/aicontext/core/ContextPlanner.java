package io.github.forgottenlab.aicontext.core;

/**
 * Produces a deterministic context-source plan for one model turn.
 */
public interface ContextPlanner {

    ContextPlan plan(ContextRequest request);
}