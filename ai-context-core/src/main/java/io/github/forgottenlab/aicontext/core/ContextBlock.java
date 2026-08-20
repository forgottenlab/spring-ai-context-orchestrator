package io.github.forgottenlab.aicontext.core;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Structured, rendered context ready for an integration layer.
 *
 * <p>ContextBlock preserves provenance and arbitration metadata instead of
 * flattening the whole context assembly into one anonymous string.</p>
 */
public record ContextBlock(
        String sourceId,
        String name,
        ContextKey key,
        String content,
        ContextPriority priority,
        ContextAuthority authority,
        Instant observedAt,
        Map<String, Object> metadata
) {

    public ContextBlock {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(priority, "priority must not be null");
        Objects.requireNonNull(authority, "authority must not be null");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}