package io.github.forgottenlab.aicontext.core;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default deterministic context budgeter.
 *
 * <p>Selection rules:</p>
 * <ol>
 *     <li>estimate each resolved candidate exactly once;</li>
 *     <li>REQUIRED context is always selected and may never be silently dropped;</li>
 *     <li>if REQUIRED context alone exceeds the budget, fail explicitly;</li>
 *     <li>remaining candidates are considered by priority:
 *         HIGH, NORMAL, then LOW;</li>
 *     <li>within the same priority, original resolution order is preserved;</li>
 *     <li>an item that does not fit is rejected, but smaller later items may still fit;</li>
 *     <li>final selected output preserves original resolution order.</li>
 * </ol>
 *
 * <p>Authority and freshness are intentionally absent here because conflicting
 * facts should already have been resolved by ContextResolver.</p>
 */
public final class DefaultContextBudgeter implements ContextBudgeter {

    private static final List<ContextPriority> OPTIONAL_PRIORITY_ORDER =
            List.of(
                    ContextPriority.HIGH,
                    ContextPriority.NORMAL,
                    ContextPriority.LOW
            );

    @Override
    public ContextBudgetResult apply(
            ContextResolution resolution,
            ContextBudgetPolicy policy,
            ContextCostEstimator estimator
    ) {
        Objects.requireNonNull(resolution, "resolution must not be null");
        Objects.requireNonNull(policy, "policy must not be null");
        Objects.requireNonNull(estimator, "estimator must not be null");

        List<ContextCandidate> candidates = resolution.selected();

        Map<ContextCandidate, Long> costs = new IdentityHashMap<>();
        Map<ContextPriority, List<ContextCandidate>> byPriority =
                new EnumMap<>(ContextPriority.class);

        for (ContextPriority priority : ContextPriority.values()) {
            byPriority.put(priority, new ArrayList<>());
        }

        long requiredCost = 0;

        for (ContextCandidate candidate : candidates) {
            Objects.requireNonNull(candidate, "candidate must not be null");

            long cost = estimator.estimate(candidate);
            if (cost < 0) {
                throw new IllegalArgumentException(
                        "ContextCostEstimator returned negative cost for source "
                                + candidate.sourceId()
                );
            }

            costs.put(candidate, cost);
            byPriority.get(candidate.item().priority()).add(candidate);

            if (candidate.item().priority() == ContextPriority.REQUIRED) {
                requiredCost = addExact(
                        requiredCost,
                        cost,
                        "required context cost overflow"
                );
            }
        }

        if (requiredCost > policy.maxCost()) {
            throw new RequiredContextBudgetExceededException(
                    requiredCost,
                    policy.maxCost()
            );
        }

        Set<ContextCandidate> selected =
                java.util.Collections.newSetFromMap(new IdentityHashMap<>());

        Set<ContextCandidate> rejected =
                java.util.Collections.newSetFromMap(new IdentityHashMap<>());

        long usedCost = 0;

        for (ContextCandidate candidate :
                byPriority.get(ContextPriority.REQUIRED)) {
            selected.add(candidate);
            usedCost = addExact(
                    usedCost,
                    costs.get(candidate),
                    "used context cost overflow"
            );
        }

        for (ContextPriority priority : OPTIONAL_PRIORITY_ORDER) {
            for (ContextCandidate candidate : byPriority.get(priority)) {
                long cost = costs.get(candidate);

                if (cost <= policy.maxCost() - usedCost) {
                    selected.add(candidate);
                    usedCost = addExact(
                            usedCost,
                            cost,
                            "used context cost overflow"
                    );
                } else {
                    rejected.add(candidate);
                }
            }
        }

        List<ContextCandidate> selectedInOriginalOrder =
                new ArrayList<>();
        List<ContextBudgetRejection> rejectedInOriginalOrder =
                new ArrayList<>();

        for (ContextCandidate candidate : candidates) {
            if (selected.contains(candidate)) {
                selectedInOriginalOrder.add(candidate);
            } else if (rejected.contains(candidate)) {
                rejectedInOriginalOrder.add(
                        new ContextBudgetRejection(
                                candidate,
                                costs.get(candidate),
                                ContextBudgetRejectionReason.BUDGET_EXCEEDED
                        )
                );
            }
        }

        return new ContextBudgetResult(
                selectedInOriginalOrder,
                rejectedInOriginalOrder,
                usedCost,
                policy.maxCost()
        );
    }

    private static long addExact(
            long left,
            long right,
            String message
    ) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(message, exception);
        }
    }
}