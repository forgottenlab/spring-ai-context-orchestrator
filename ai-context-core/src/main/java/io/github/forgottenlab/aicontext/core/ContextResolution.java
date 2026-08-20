package io.github.forgottenlab.aicontext.core;

import java.util.List;

/**
 * Immutable output of context resolution.
 */
public record ContextResolution(
        List<ContextCandidate> selected,
        List<ContextRejection> rejected
) {

    public ContextResolution {
        selected = selected == null ? List.of() : List.copyOf(selected);
        rejected = rejected == null ? List.of() : List.copyOf(rejected);
    }

    public List<ContextItem<?>> selectedItems() {
        return selected.stream()
                .map(ContextCandidate::item)
                .toList();
    }

    public boolean hasRejections() {
        return !rejected.isEmpty();
    }
}