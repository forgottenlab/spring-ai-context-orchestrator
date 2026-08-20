package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class DefaultContextPlannerTest {

    private static final ContextRequest REQUEST =
            new ContextRequest("hello", "conversation-1", Map.of());

    @Test
    void alwaysStrategyLoadsSupportedSource() {
        ContextPlan plan = planner(
                source("always", ContextLoadStrategy.ALWAYS, true, true, ContextPlanDecision.SKIP)
        ).plan(REQUEST);

        assertEquals(List.of("always"), plan.loadSourceIds());
        assertTrue(plan.deferredSourceIds().isEmpty());
        assertTrue(plan.skippedSourceIds().isEmpty());
    }

    @Test
    void unsupportedSourceIsSkippedBeforeStrategyEvaluation() {
        ContextPlan plan = planner(
                source("unsupported", ContextLoadStrategy.ON_DEMAND, false, true, ContextPlanDecision.DEFER)
        ).plan(REQUEST);

        assertEquals(List.of("unsupported"), plan.skippedSourceIds());
        assertTrue(plan.loadSourceIds().isEmpty());
        assertTrue(plan.deferredSourceIds().isEmpty());
    }

    @Test
    void relevantStrategyLoadsOnlyRelevantSource() {
        ContextPlan plan = planner(
                source("relevant", ContextLoadStrategy.RELEVANT, true, true, ContextPlanDecision.SKIP),
                source("irrelevant", ContextLoadStrategy.RELEVANT, true, false, ContextPlanDecision.LOAD)
        ).plan(REQUEST);

        assertEquals(List.of("relevant"), plan.loadSourceIds());
        assertEquals(List.of("irrelevant"), plan.skippedSourceIds());
    }

    @Test
    void onDemandStrategyDefersSupportedSource() {
        ContextPlan plan = planner(
                source("inventory", ContextLoadStrategy.ON_DEMAND, true, true, ContextPlanDecision.LOAD)
        ).plan(REQUEST);

        assertEquals(List.of("inventory"), plan.deferredSourceIds());
        assertTrue(plan.loadSourceIds().isEmpty());
    }

    @Test
    void customStrategyHonorsExplicitDecision() {
        ContextPlan plan = planner(
                source("custom-load", ContextLoadStrategy.CUSTOM, true, true, ContextPlanDecision.LOAD),
                source("custom-defer", ContextLoadStrategy.CUSTOM, true, true, ContextPlanDecision.DEFER),
                source("custom-skip", ContextLoadStrategy.CUSTOM, true, true, ContextPlanDecision.SKIP)
        ).plan(REQUEST);

        assertEquals(List.of("custom-load"), plan.loadSourceIds());
        assertEquals(List.of("custom-defer"), plan.deferredSourceIds());
        assertEquals(List.of("custom-skip"), plan.skippedSourceIds());
    }

    @Test
    void preservesRegistryOrderWithinPlan() {
        ContextPlan plan = planner(
                source("first", ContextLoadStrategy.ALWAYS, true, true, ContextPlanDecision.LOAD),
                source("second", ContextLoadStrategy.ON_DEMAND, true, true, ContextPlanDecision.DEFER),
                source("third", ContextLoadStrategy.RELEVANT, true, false, ContextPlanDecision.SKIP)
        ).plan(REQUEST);

        assertEquals(
                List.of("first", "second", "third"),
                plan.entries().stream().map(ContextPlanEntry::sourceId).toList()
        );
    }

    @Test
    void planEntriesAreImmutable() {
        ContextPlan plan = planner(
                source("first", ContextLoadStrategy.ALWAYS, true, true, ContextPlanDecision.LOAD)
        ).plan(REQUEST);

        assertThrows(
                UnsupportedOperationException.class,
                () -> plan.entries().add(
                        new ContextPlanEntry(
                                "illegal",
                                ContextLoadStrategy.ALWAYS,
                                ContextPlanDecision.LOAD
                        )
                )
        );
    }

    @Test
    void rejectsNullRequest() {
        DefaultContextPlanner planner = planner(
                source("first", ContextLoadStrategy.ALWAYS, true, true, ContextPlanDecision.LOAD)
        );

        assertThrows(NullPointerException.class, () -> planner.plan(null));
    }

    @Test
    void rejectsNullCustomDecision() {
        ContextSource source = new TestSource(
                "broken",
                ContextLoadStrategy.CUSTOM,
                true,
                true,
                null
        );

        DefaultContextPlanner planner = planner(source);

        assertThrows(NullPointerException.class, () -> planner.plan(REQUEST));
    }

    private static DefaultContextPlanner planner(ContextSource... sources) {
        return new DefaultContextPlanner(new ContextRegistry(List.of(sources)));
    }

    private static ContextSource source(
            String id,
            ContextLoadStrategy strategy,
            boolean supported,
            boolean relevant,
            ContextPlanDecision customDecision
    ) {
        return new TestSource(id, strategy, supported, relevant, customDecision);
    }

    private record TestSource(
            String id,
            ContextLoadStrategy strategy,
            boolean supported,
            boolean relevant,
            ContextPlanDecision customDecision
    ) implements ContextSource {

        @Override
        public ContextLoadStrategy loadStrategy() {
            return strategy;
        }

        @Override
        public boolean supports(ContextRequest request) {
            return supported;
        }

        @Override
        public boolean isRelevant(ContextRequest request) {
            return relevant;
        }

        @Override
        public ContextPlanDecision customPlan(ContextRequest request) {
            return customDecision;
        }

        @Override
        public CompletableFuture<ContextContribution> load(ContextRequest request) {
            return CompletableFuture.completedFuture(ContextContribution.empty(id));
        }
    }
}