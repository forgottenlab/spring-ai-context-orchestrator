package io.github.forgottenlab.aicontext.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * One typed contribution to the model context.
 *
 * @param name logical item name
 * @param value typed value; rendering is intentionally deferred
 * @param priority budget priority
 * @param authority conflict-resolution authority
 * @param observedAt when the value was observed or loaded
 * @param maxAge optional validity window; null means source-defined
 * @param metadata source-specific metadata
 * @param <T> value type
 */
public record ContextItem<T>(
        String name,
        T value,
        ContextPriority priority,
        ContextAuthority authority,
        Instant observedAt,
        Duration maxAge,
        Map<String, Object> metadata
) {

    public ContextItem {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(priority, "priority must not be null");
        Objects.requireNonNull(authority, "authority must not be null");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return observedAt != null
                && maxAge != null
                && now.isAfter(observedAt.plus(maxAge));
    }
}