package io.github.forgottenlab.aicontext.core.planning;

import java.util.List;
import java.util.Objects;

/**
 * Selection-only plan for one context-loading phase.
 *
 * <p>This type deliberately does not execute sources. Planning and retrieval
 * remain separate responsibilities.</p>
 */
public record ContextPlan(List<ContextPlanEntry> entries) {

    public ContextPlan {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public List<String> loadSourceIds() {
        return sourceIds(ContextPlanDecision.LOAD);
    }

    public List<String> deferredSourceIds() {
        return sourceIds(ContextPlanDecision.DEFER);
    }

    public List<String> skippedSourceIds() {
        return sourceIds(ContextPlanDecision.SKIP);
    }

    private List<String> sourceIds(ContextPlanDecision decision) {
        Objects.requireNonNull(decision, "decision must not be null");
        return entries.stream()
                .filter(entry -> entry.decision() == decision)
                .map(ContextPlanEntry::sourceId)
                .toList();
    }
}