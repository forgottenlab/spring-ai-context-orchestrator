package io.github.forgottenlab.aicontext.core.planning;

import io.github.forgottenlab.aicontext.core.ContextLoadStrategy;
import io.github.forgottenlab.aicontext.core.ContextRegistry;
import io.github.forgottenlab.aicontext.core.ContextRequest;
import io.github.forgottenlab.aicontext.core.ContextSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default deterministic planner.
 *
 * <p>The planner only decides source participation. It does not execute any
 * source, enforce timeouts, apply budgets, or render model messages.</p>
 */
public final class DefaultContextPlanner implements ContextPlanner {

    private final ContextRegistry registry;

    public DefaultContextPlanner(ContextRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    @Override
    public ContextPlan plan(ContextRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        List<ContextPlanEntry> entries = new ArrayList<>();

        for (ContextSource source : registry.all()) {
            ContextLoadStrategy strategy = Objects.requireNonNull(
                    source.loadStrategy(),
                    "ContextSource.loadStrategy() must not return null: " + source.id()
            );

            ContextPlanDecision decision = decide(source, strategy, request);
            entries.add(new ContextPlanEntry(source.id(), strategy, decision));
        }

        return new ContextPlan(entries);
    }

    private ContextPlanDecision decide(
            ContextSource source,
            ContextLoadStrategy strategy,
            ContextRequest request
    ) {
        if (!source.supports(request)) {
            return ContextPlanDecision.SKIP;
        }

        return switch (strategy) {
            case ALWAYS -> ContextPlanDecision.LOAD;
            case RELEVANT -> source.isRelevant(request)
                    ? ContextPlanDecision.LOAD
                    : ContextPlanDecision.SKIP;
            case ON_DEMAND -> ContextPlanDecision.DEFER;
            case CUSTOM -> Objects.requireNonNull(
                    source.customPlan(request),
                    "ContextSource.customPlan() must not return null: " + source.id()
            );
        };
    }
}