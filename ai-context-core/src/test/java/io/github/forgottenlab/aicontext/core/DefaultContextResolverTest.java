package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultContextResolverTest {

    private static final Instant NOW =
            Instant.parse("2026-08-21T00:00:00Z");

    private final ContextResolver resolver =
            new DefaultContextResolver();

    @Test
    void differentKeysDoNotConflict() {
        ContextItem<String> productOne = keyed(
                "product-1-price",
                "499",
                ContextKey.of("product", "1", "price"),
                ContextPriority.NORMAL,
                ContextAuthority.TRUSTED,
                NOW.minusSeconds(10),
                null
        );

        ContextItem<String> productTwo = keyed(
                "product-2-price",
                "699",
                ContextKey.of("product", "2", "price"),
                ContextPriority.NORMAL,
                ContextAuthority.TRUSTED,
                NOW.minusSeconds(10),
                null
        );

        ContextResolution resolution = resolver.resolve(
                List.of(contribution("mysql", productOne, productTwo)),
                NOW
        );

        assertEquals(2, resolution.selected().size());
        assertTrue(resolution.rejected().isEmpty());
    }

    @Test
    void unkeyedItemsWithSameNameArePreserved() {
        ContextItem<String> first = unkeyed(
                "document",
                "chunk one"
        );

        ContextItem<String> second = unkeyed(
                "document",
                "chunk two"
        );

        ContextResolution resolution = resolver.resolve(
                List.of(contribution("vector", first, second)),
                NOW
        );

        assertEquals(2, resolution.selected().size());
        assertTrue(resolution.rejected().isEmpty());
    }

    @Test
    void higherAuthorityWinsEvenWhenLowerPriorityAndOlder() {
        ContextKey key =
                ContextKey.of("product", "42", "price");

        ContextItem<String> database = keyed(
                "db-price",
                "499",
                key,
                ContextPriority.LOW,
                ContextAuthority.AUTHORITATIVE,
                NOW.minusSeconds(60),
                null
        );

        ContextItem<String> memory = keyed(
                "memory-price",
                "599",
                key,
                ContextPriority.REQUIRED,
                ContextAuthority.USER_PROVIDED,
                NOW.minusSeconds(5),
                null
        );

        ContextResolution resolution = resolver.resolve(
                List.of(
                        contribution("mysql", database),
                        contribution("memory", memory)
                ),
                NOW
        );

        assertEquals(1, resolution.selected().size());
        assertEquals(
                "499",
                resolution.selected().get(0).item().value()
        );

        assertEquals(1, resolution.rejected().size());
        assertEquals(
                ContextRejectionReason.SUPERSEDED,
                resolution.rejected().get(0).reason()
        );

        assertEquals(
                "mysql",
                resolution.rejected().get(0).winner().sourceId()
        );
    }

    @Test
    void newerObservationWinsWhenAuthorityIsEqual() {
        ContextKey key =
                ContextKey.of("inventory", "sku-1", "available");

        ContextItem<Boolean> older = keyed(
                "older",
                false,
                key,
                ContextPriority.NORMAL,
                ContextAuthority.AUTHORITATIVE,
                NOW.minusSeconds(30),
                null
        );

        ContextItem<Boolean> newer = keyed(
                "newer",
                true,
                key,
                ContextPriority.NORMAL,
                ContextAuthority.AUTHORITATIVE,
                NOW.minusSeconds(5),
                null
        );

        ContextResolution resolution = resolver.resolve(
                List.of(
                        contribution("cache", older),
                        contribution("database", newer)
                ),
                NOW
        );

        assertEquals(
                true,
                resolution.selected().get(0).item().value()
        );
        assertEquals(
                "database",
                resolution.selected().get(0).sourceId()
        );
    }

    @Test
    void nonNullObservationWinsAgainstUnknownAgeAtEqualAuthority() {
        ContextKey key =
                ContextKey.of("product", "42", "stock");

        ContextItem<Integer> unknownAge = keyed(
                "unknown",
                3,
                key,
                ContextPriority.NORMAL,
                ContextAuthority.TRUSTED,
                null,
                null
        );

        ContextItem<Integer> observed = keyed(
                "observed",
                5,
                key,
                ContextPriority.NORMAL,
                ContextAuthority.TRUSTED,
                NOW.minusSeconds(10),
                null
        );

        ContextResolution resolution = resolver.resolve(
                List.of(
                        contribution("unknown-source", unknownAge),
                        contribution("observed-source", observed)
                ),
                NOW
        );

        assertEquals(
                "observed-source",
                resolution.selected().get(0).sourceId()
        );
    }

    @Test
    void expiredItemsAreRejectedBeforeCompetition() {
        ContextKey key =
                ContextKey.of("inventory", "sku-1", "available");

        ContextItem<Boolean> expiredAuthoritative = keyed(
                "expired",
                true,
                key,
                ContextPriority.REQUIRED,
                ContextAuthority.AUTHORITATIVE,
                NOW.minusSeconds(120),
                Duration.ofSeconds(30)
        );

        ContextItem<Boolean> validTrusted = keyed(
                "valid",
                false,
                key,
                ContextPriority.NORMAL,
                ContextAuthority.TRUSTED,
                NOW.minusSeconds(5),
                Duration.ofMinutes(1)
        );

        ContextResolution resolution = resolver.resolve(
                List.of(
                        contribution("old-db", expiredAuthoritative),
                        contribution("fresh-cache", validTrusted)
                ),
                NOW
        );

        assertEquals(1, resolution.selected().size());
        assertEquals(
                "fresh-cache",
                resolution.selected().get(0).sourceId()
        );

        assertEquals(1, resolution.rejected().size());
        assertEquals(
                ContextRejectionReason.EXPIRED,
                resolution.rejected().get(0).reason()
        );
        assertNull(resolution.rejected().get(0).winner());
    }

    @Test
    void completeTiePreservesFirstSeenCandidate() {
        ContextKey key =
                ContextKey.of("product", "42", "category");

        ContextItem<String> first = keyed(
                "first",
                "electronics",
                key,
                ContextPriority.NORMAL,
                ContextAuthority.TRUSTED,
                NOW.minusSeconds(5),
                null
        );

        ContextItem<String> second = keyed(
                "second",
                "phones",
                key,
                ContextPriority.REQUIRED,
                ContextAuthority.TRUSTED,
                NOW.minusSeconds(5),
                null
        );

        ContextResolution resolution = resolver.resolve(
                List.of(
                        contribution("first-source", first),
                        contribution("second-source", second)
                ),
                NOW
        );

        assertEquals(
                "first-source",
                resolution.selected().get(0).sourceId()
        );
    }

    @Test
    void multipleCompetitionRoundsTrackEachSupersededCandidate() {
        ContextKey key =
                ContextKey.of("product", "42", "price");

        ContextItem<String> first = keyed(
                "first",
                "599",
                key,
                ContextPriority.NORMAL,
                ContextAuthority.UNVERIFIED,
                NOW.minusSeconds(30),
                null
        );

        ContextItem<String> second = keyed(
                "second",
                "549",
                key,
                ContextPriority.NORMAL,
                ContextAuthority.TRUSTED,
                NOW.minusSeconds(20),
                null
        );

        ContextItem<String> third = keyed(
                "third",
                "499",
                key,
                ContextPriority.NORMAL,
                ContextAuthority.AUTHORITATIVE,
                NOW.minusSeconds(10),
                null
        );

        ContextResolution resolution = resolver.resolve(
                List.of(
                        contribution("web", first),
                        contribution("cache", second),
                        contribution("mysql", third)
                ),
                NOW
        );

        assertEquals(1, resolution.selected().size());
        assertEquals(
                "mysql",
                resolution.selected().get(0).sourceId()
        );

        assertEquals(2, resolution.rejected().size());
        assertEquals(
                List.of("web", "cache"),
                resolution.rejected().stream()
                        .map(rejection -> rejection.candidate().sourceId())
                        .toList()
        );
    }

    private static ContextContribution contribution(
            String sourceId,
            ContextItem<?>... items
    ) {
        return new ContextContribution(
                sourceId,
                List.of(items)
        );
    }

    private static ContextItem<String> unkeyed(
            String name,
            String value
    ) {
        return new ContextItem<>(
                name,
                value,
                ContextPriority.NORMAL,
                ContextAuthority.TRUSTED,
                NOW.minusSeconds(5),
                null,
                Map.of()
        );
    }

    private static <T> ContextItem<T> keyed(
            String name,
            T value,
            ContextKey key,
            ContextPriority priority,
            ContextAuthority authority,
            Instant observedAt,
            Duration maxAge
    ) {
        return new ContextItem<>(
                name,
                value,
                key,
                priority,
                authority,
                observedAt,
                maxAge,
                Map.of()
        );
    }
}