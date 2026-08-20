package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextContributionTest {

    @Test
    void copiesAndProtectsItems() {
        List<ContextItem<?>> items = new ArrayList<>();
        items.add(sampleItem("first"));

        ContextContribution contribution = new ContextContribution("products", items);

        items.add(sampleItem("second"));

        assertEquals(1, contribution.items().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> contribution.items().add(sampleItem("illegal"))
        );
    }

    @Test
    void convertsNullItemsToEmptyList() {
        ContextContribution contribution = new ContextContribution("products", null);

        assertTrue(contribution.items().isEmpty());
    }

    @Test
    void emptyFactoryPreservesSourceId() {
        ContextContribution contribution = ContextContribution.empty("products");

        assertEquals("products", contribution.sourceId());
        assertTrue(contribution.items().isEmpty());
    }

    @Test
    void rejectsNullSourceId() {
        assertThrows(
                NullPointerException.class,
                () -> new ContextContribution(null, List.of())
        );
    }

    private static ContextItem<String> sampleItem(String value) {
        return new ContextItem<>(
                "sample",
                value,
                ContextPriority.NORMAL,
                ContextAuthority.TRUSTED,
                Instant.parse("2026-08-21T00:00:00Z"),
                null,
                Map.of()
        );
    }
}