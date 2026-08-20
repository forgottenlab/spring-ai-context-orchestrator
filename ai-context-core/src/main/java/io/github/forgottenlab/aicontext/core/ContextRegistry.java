package io.github.forgottenlab.aicontext.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Registry of context sources visible to the orchestration layer.
 */
public final class ContextRegistry {

    private final Map<String, ContextSource> sources;

    public ContextRegistry(Collection<? extends ContextSource> sources) {
        Objects.requireNonNull(sources, "sources must not be null");

        Map<String, ContextSource> indexed = new LinkedHashMap<>();
        for (ContextSource source : sources) {
            ContextSource previous = indexed.putIfAbsent(source.id(), source);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "Duplicate ContextSource id: " + source.id()
                );
            }
        }
        this.sources = Map.copyOf(indexed);
    }

    public Optional<ContextSource> find(String id) {
        return Optional.ofNullable(sources.get(id));
    }

    public List<ContextSource> all() {
        return List.copyOf(sources.values());
    }
}