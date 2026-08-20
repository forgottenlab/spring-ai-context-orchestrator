package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextItemTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-21T00:00:00Z");

    @Test
    void copiesAndProtectsMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "mysql");

        ContextItem<String> item = item(metadata);

        metadata.put("source", "changed");
        assertEquals("mysql", item.metadata().get("source"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> item.metadata().put("illegal", true)
        );
    }

    @Test
    void expiresOnlyAfterMaxAgeBoundary() {
        ContextItem<String> item = item(Map.of());

        assertFalse(item.isExpired(OBSERVED_AT.plus(Duration.ofMinutes(5))));
        assertTrue(item.isExpired(
                OBSERVED_AT.plus(Duration.ofMinutes(5)).plusNanos(1)
        ));
    }

    @Test
    void doesNotExpireWhenFreshnessFactsAreIncomplete() {
        ContextItem<String> noTimestamp = new ContextItem<>(
                "price",
                "499",
                ContextPriority.HIGH,
                ContextAuthority.AUTHORITATIVE,
                null,
                Duration.ofMinutes(5),
                Map.of()
        );

        ContextItem<String> noMaxAge = new ContextItem<>(
                "price",
                "499",
                ContextPriority.HIGH,
                ContextAuthority.AUTHORITATIVE,
                OBSERVED_AT,
                null,
                Map.of()
        );

        assertFalse(noTimestamp.isExpired(OBSERVED_AT.plus(Duration.ofDays(1))));
        assertFalse(noMaxAge.isExpired(OBSERVED_AT.plus(Duration.ofDays(1))));
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThrows(NullPointerException.class, () -> new ContextItem<>(
                null, "499", ContextPriority.HIGH, ContextAuthority.AUTHORITATIVE,
                OBSERVED_AT, Duration.ofMinutes(5), Map.of()
        ));

        assertThrows(NullPointerException.class, () -> new ContextItem<>(
                "price", null, ContextPriority.HIGH, ContextAuthority.AUTHORITATIVE,
                OBSERVED_AT, Duration.ofMinutes(5), Map.of()
        ));

        assertThrows(NullPointerException.class, () -> new ContextItem<>(
                "price", "499", null, ContextAuthority.AUTHORITATIVE,
                OBSERVED_AT, Duration.ofMinutes(5), Map.of()
        ));

        assertThrows(NullPointerException.class, () -> new ContextItem<>(
                "price", "499", ContextPriority.HIGH, null,
                OBSERVED_AT, Duration.ofMinutes(5), Map.of()
        ));
    }

    private static ContextItem<String> item(Map<String, Object> metadata) {
        return new ContextItem<>(
                "price",
                "499",
                ContextPriority.HIGH,
                ContextAuthority.AUTHORITATIVE,
                OBSERVED_AT,
                Duration.ofMinutes(5),
                metadata
        );
    }
}