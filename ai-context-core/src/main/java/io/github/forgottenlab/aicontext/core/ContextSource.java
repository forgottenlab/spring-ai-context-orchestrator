package io.github.forgottenlab.aicontext.core;

import java.util.concurrent.CompletableFuture;

/**
 * Extension point for any business context source.
 *
 * <p>The core uses CompletableFuture rather than Reactor so providers can stay
 * independent from Spring while still supporting concurrent retrieval.</p>
 */
public interface ContextSource {

    String id();

    /**
     * Defines the default planning behavior for this source.
     */
    default ContextLoadStrategy loadStrategy() {
        return ContextLoadStrategy.ALWAYS;
    }

    /**
     * Defines execution constraints for this source.
     */
    default ContextExecutionPolicy executionPolicy() {
        return ContextExecutionPolicy.defaults();
    }

    /**
     * Technical eligibility guard. Returning false always causes the planner
     * to skip the source for the current request.
     */
    default boolean supports(ContextRequest request) {
        return true;
    }

    /**
     * Relevance hook used only when {@link #loadStrategy()} is RELEVANT.
     */
    default boolean isRelevant(ContextRequest request) {
        return true;
    }

    /**
     * Explicit planning hook used only when {@link #loadStrategy()} is CUSTOM.
     */
    default ContextPlanDecision customPlan(ContextRequest request) {
        return ContextPlanDecision.LOAD;
    }

    CompletableFuture<ContextContribution> load(ContextRequest request);
}