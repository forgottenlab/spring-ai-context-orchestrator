package io.github.forgottenlab.aicontext.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Registry of context sources visible to the orchestration layer.
 *
 * <p>Registration order is preserved so later planning stages can behave
 * deterministically when no stronger ordering policy is configured.</p>
 */
public final class ContextRegistry {

    private final Map<String, ContextSource> sources;
    private final List<ContextSource> orderedSources;

    public ContextRegistry(Collection<? extends ContextSource> sources) {
        Objects.requireNonNull(sources, "sources must not be null");

        Map<String, ContextSource> indexed = new LinkedHashMap<>();
        List<ContextSource> ordered = new ArrayList<>();

        for (ContextSource source : sources) {
            Objects.requireNonNull(source, "context source must not be null");

            String id = Objects.requireNonNull(source.id(), "context source id must not be null");
            if (id.isBlank()) {
                throw new IllegalArgumentException("ContextSource id must not be blank");
            }

            ContextSource previous = indexed.putIfAbsent(id, source);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate ContextSource id: " + id);
            }

            ordered.add(source);
        }

        this.sources = Collections.unmodifiableMap(new LinkedHashMap<>(indexed));
        this.orderedSources = List.copyOf(ordered);
    }

    public Optional<ContextSource> find(String id) {
        return Optional.ofNullable(sources.get(id));
    }

    public List<ContextSource> all() {
        return orderedSources;
    }
}