package io.github.forgottenlab.aicontext.core.budget;

import io.github.forgottenlab.aicontext.core.ContextAuthority;
import io.github.forgottenlab.aicontext.core.ContextCandidate;
import io.github.forgottenlab.aicontext.core.ContextItem;
import io.github.forgottenlab.aicontext.core.ContextPriority;
import io.github.forgottenlab.aicontext.core.resolution.ContextResolution;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DefaultContextBudgeterTest {

    private static final Instant NOW =
            Instant.parse("2026-08-21T00:00:00Z");

    private final ContextBudgeter budgeter =
            new DefaultContextBudgeter();

    @Test
    void requiredContextIsAlwaysSelected() {
        ContextCandidate required =
                candidate("required", ContextPriority.REQUIRED);

        ContextCandidate low =
                candidate("low", ContextPriority.LOW);

        ContextBudgetResult result = apply(
                List.of(required, low),
                5,
                costs(
                        entry(required, 4),
                        entry(low, 2)
                )
        );

        assertEquals(
                List.of(required),
                result.selected()
        );

        assertEquals(4, result.usedCost());
        assertEquals(1, result.rejected().size());
        assertEquals(low, result.rejected().get(0).candidate());
    }

    @Test
    void requiredOverflowFailsInsteadOfSilentlyDroppingContext() {
        ContextCandidate first =
                candidate("required-1", ContextPriority.REQUIRED);

        ContextCandidate second =
                candidate("required-2", ContextPriority.REQUIRED);

        RequiredContextBudgetExceededException exception =
                assertThrows(
                        RequiredContextBudgetExceededException.class,
                        () -> apply(
                                List.of(first, second),
                                5,
                                costs(
                                        entry(first, 3),
                                        entry(second, 3)
                                )
                        )
                );

        assertEquals(6, exception.requiredCost());
        assertEquals(5, exception.maxCost());
    }

    @Test
    void higherPriorityIsConsideredBeforeLowerPriority() {
        ContextCandidate low =
                candidate("low", ContextPriority.LOW);

        ContextCandidate high =
                candidate("high", ContextPriority.HIGH);

        ContextBudgetResult result = apply(
                List.of(low, high),
                5,
                costs(
                        entry(low, 5),
                        entry(high, 5)
                )
        );

        assertEquals(List.of(high), result.selected());
        assertEquals(low, result.rejected().get(0).candidate());
    }

    @Test
    void normalPriorityBeatsLowPriority() {
        ContextCandidate low =
                candidate("low", ContextPriority.LOW);

        ContextCandidate normal =
                candidate("normal", ContextPriority.NORMAL);

        ContextBudgetResult result = apply(
                List.of(low, normal),
                4,
                costs(
                        entry(low, 4),
                        entry(normal, 4)
                )
        );

        assertEquals(List.of(normal), result.selected());
        assertEquals(low, result.rejected().get(0).candidate());
    }

    @Test
    void samePriorityPreservesOriginalOrder() {
        ContextCandidate first =
                candidate("first", ContextPriority.HIGH);

        ContextCandidate second =
                candidate("second", ContextPriority.HIGH);

        ContextBudgetResult result = apply(
                List.of(first, second),
                5,
                costs(
                        entry(first, 5),
                        entry(second, 5)
                )
        );

        assertEquals(List.of(first), result.selected());
        assertEquals(second, result.rejected().get(0).candidate());
    }

    @Test
    void oversizedOptionalItemDoesNotBlockSmallerLaterItem() {
        ContextCandidate large =
                candidate("large", ContextPriority.HIGH);

        ContextCandidate small =
                candidate("small", ContextPriority.HIGH);

        ContextBudgetResult result = apply(
                List.of(large, small),
                4,
                costs(
                        entry(large, 5),
                        entry(small, 3)
                )
        );

        assertEquals(List.of(small), result.selected());
        assertEquals(large, result.rejected().get(0).candidate());
        assertEquals(3, result.usedCost());
    }

    @Test
    void finalSelectedOutputPreservesResolutionOrder() {
        ContextCandidate low =
                candidate("low", ContextPriority.LOW);

        ContextCandidate required =
                candidate("required", ContextPriority.REQUIRED);

        ContextCandidate high =
                candidate("high", ContextPriority.HIGH);

        ContextBudgetResult result = apply(
                List.of(low, required, high),
                10,
                costs(
                        entry(low, 1),
                        entry(required, 2),
                        entry(high, 3)
                )
        );

        assertEquals(
                List.of(low, required, high),
                result.selected()
        );
    }

    @Test
    void zeroCostContextFitsWithoutConsumingBudget() {
        ContextCandidate free =
                candidate("free", ContextPriority.LOW);

        ContextBudgetResult result = apply(
                List.of(free),
                1,
                costs(entry(free, 0))
        );

        assertEquals(List.of(free), result.selected());
        assertEquals(0, result.usedCost());
        assertEquals(1, result.remainingCost());
    }

    @Test
    void estimatorIsCalledExactlyOncePerCandidate() {
        ContextCandidate first =
                candidate("first", ContextPriority.HIGH);

        ContextCandidate second =
                candidate("second", ContextPriority.NORMAL);

        AtomicInteger calls = new AtomicInteger();

        ContextCostEstimator estimator = candidate -> {
            calls.incrementAndGet();
            return 1;
        };

        ContextResolution resolution =
                new ContextResolution(
                        List.of(first, second),
                        List.of()
                );

        budgeter.apply(
                resolution,
                new ContextBudgetPolicy(10),
                estimator
        );

        assertEquals(2, calls.get());
    }

    @Test
    void negativeEstimatorCostIsRejected() {
        ContextCandidate candidate =
                candidate("bad", ContextPriority.NORMAL);

        ContextResolution resolution =
                new ContextResolution(
                        List.of(candidate),
                        List.of()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> budgeter.apply(
                        resolution,
                        new ContextBudgetPolicy(10),
                        ignored -> -1
                )
        );
    }

    @Test
    void doesNotUseAuthorityToOverridePriorityAtBudgetStage() {
        ContextCandidate highUnverified =
                candidate(
                        "high-unverified",
                        ContextPriority.HIGH,
                        ContextAuthority.UNVERIFIED
                );

        ContextCandidate lowAuthoritative =
                candidate(
                        "low-authoritative",
                        ContextPriority.LOW,
                        ContextAuthority.AUTHORITATIVE
                );

        ContextBudgetResult result = apply(
                List.of(lowAuthoritative, highUnverified),
                5,
                costs(
                        entry(lowAuthoritative, 5),
                        entry(highUnverified, 5)
                )
        );

        assertEquals(
                List.of(highUnverified),
                result.selected()
        );
    }

    @Test
    void rejectedDiagnosticsPreserveOriginalOrder() {
        ContextCandidate lowOne =
                candidate("low-1", ContextPriority.LOW);

        ContextCandidate high =
                candidate("high", ContextPriority.HIGH);

        ContextCandidate lowTwo =
                candidate("low-2", ContextPriority.LOW);

        ContextBudgetResult result = apply(
                List.of(lowOne, high, lowTwo),
                5,
                costs(
                        entry(lowOne, 5),
                        entry(high, 5),
                        entry(lowTwo, 5)
                )
        );

        assertEquals(
                List.of(lowOne, lowTwo),
                result.rejected().stream()
                        .map(ContextBudgetRejection::candidate)
                        .toList()
        );
    }

    @Test
    void rejectsNullInputs() {
        ContextResolution resolution =
                new ContextResolution(List.of(), List.of());

        ContextBudgetPolicy policy =
                new ContextBudgetPolicy(10);

        ContextCostEstimator estimator =
                candidate -> 1;

        assertThrows(
                NullPointerException.class,
                () -> budgeter.apply(null, policy, estimator)
        );

        assertThrows(
                NullPointerException.class,
                () -> budgeter.apply(resolution, null, estimator)
        );

        assertThrows(
                NullPointerException.class,
                () -> budgeter.apply(resolution, policy, null)
        );
    }

    private ContextBudgetResult apply(
            List<ContextCandidate> candidates,
            long maxCost,
            ContextCostEstimator estimator
    ) {
        return budgeter.apply(
                new ContextResolution(candidates, List.of()),
                new ContextBudgetPolicy(maxCost),
                estimator
        );
    }

    private static ContextCandidate candidate(
            String id,
            ContextPriority priority
    ) {
        return candidate(
                id,
                priority,
                ContextAuthority.TRUSTED
        );
    }

    private static ContextCandidate candidate(
            String id,
            ContextPriority priority,
            ContextAuthority authority
    ) {
        return new ContextCandidate(
                "source-" + id,
                new ContextItem<>(
                        id,
                        id,
                        priority,
                        authority,
                        NOW,
                        null,
                        Map.of()
                )
        );
    }

    private static CostEntry entry(
            ContextCandidate candidate,
            long cost
    ) {
        return new CostEntry(candidate, cost);
    }

    private static ContextCostEstimator costs(
            CostEntry... entries
    ) {
        Map<ContextCandidate, Long> costs =
                new java.util.IdentityHashMap<>();

        for (CostEntry entry : entries) {
            costs.put(entry.candidate(), entry.cost());
        }

        return candidate -> {
            Long cost = costs.get(candidate);
            if (cost == null) {
                fail("No test cost configured for " + candidate.sourceId());
            }
            return cost;
        };
    }

    private record CostEntry(
            ContextCandidate candidate,
            long cost
    ) {
    }
}