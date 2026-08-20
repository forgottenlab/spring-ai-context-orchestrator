package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextBudgetResultTest {

    @Test
    void protectsCollectionsAndComputesRemainingCost() {
        ContextCandidate candidate = candidate("source", "item");

        List<ContextCandidate> selected =
                new ArrayList<>(List.of(candidate));

        ContextBudgetResult result =
                new ContextBudgetResult(
                        selected,
                        new ArrayList<>(),
                        4,
                        10
                );

        selected.clear();

        assertEquals(1, result.selected().size());
        assertEquals(6, result.remainingCost());
        assertFalse(result.hasRejections());

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.selected().add(candidate)
        );
    }

    @Test
    void validatesCostBounds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ContextBudgetResult(
                        List.of(),
                        List.of(),
                        -1,
                        10
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ContextBudgetResult(
                        List.of(),
                        List.of(),
                        11,
                        10
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ContextBudgetResult(
                        List.of(),
                        List.of(),
                        0,
                        0
                )
        );
    }

    private static ContextCandidate candidate(
            String sourceId,
            String name
    ) {
        return new ContextCandidate(
                sourceId,
                new ContextItem<>(
                        name,
                        name,
                        ContextPriority.NORMAL,
                        ContextAuthority.TRUSTED,
                        Instant.parse("2026-08-21T00:00:00Z"),
                        null,
                        Map.of()
                )
        );
    }
}