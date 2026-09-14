package io.github.forgottenlab.aicontext.core.execution;

import io.github.forgottenlab.aicontext.core.ContextContribution;
import io.github.forgottenlab.aicontext.core.planning.ContextPlan;
import java.util.List;

/**
 * Immutable report for one context execution phase.
 *
 * <p>Only sources planned as LOAD appear in this report. Deferred and skipped
 * sources remain represented by the original ContextPlan.</p>
 */
public record ContextExecutionReport(List<ContextExecutionResult> results) {

    public ContextExecutionReport {
        results = results == null ? List.of() : List.copyOf(results);
    }

    public List<ContextExecutionSuccess> successes() {
        return results.stream()
                .filter(ContextExecutionSuccess.class::isInstance)
                .map(ContextExecutionSuccess.class::cast)
                .toList();
    }

    public List<ContextExecutionFailure> failures() {
        return results.stream()
                .filter(ContextExecutionFailure.class::isInstance)
                .map(ContextExecutionFailure.class::cast)
                .toList();
    }

    public List<ContextContribution> contributions() {
        return successes().stream()
                .map(ContextExecutionSuccess::contribution)
                .toList();
    }

    public boolean hasFailures() {
        return results.stream().anyMatch(result -> !result.isSuccess());
    }
}