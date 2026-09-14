package io.github.forgottenlab.aicontext.core.assembly;

import io.github.forgottenlab.aicontext.core.ContextAuthority;
import io.github.forgottenlab.aicontext.core.ContextCandidate;
import io.github.forgottenlab.aicontext.core.ContextItem;
import io.github.forgottenlab.aicontext.core.ContextPriority;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultContextValueRendererTest {

    private final ContextValueRenderer renderer =
            new DefaultContextValueRenderer();

    @Test
    void rendersStringValueWithoutChangingContent() {
        ContextCandidate candidate =
                candidate("hello world");

        assertEquals(
                "hello world",
                renderer.render(candidate)
        );
    }

    @Test
    void rendersScalarValueUsingStringRepresentation() {
        ContextCandidate candidate =
                candidate(42);

        assertEquals(
                "42",
                renderer.render(candidate)
        );
    }

    @Test
    void rendersRecordUsingItsStringRepresentation() {
        Product product =
                new Product(42L, "Keyboard");

        ContextCandidate candidate =
                candidate(product);

        assertEquals(
                product.toString(),
                renderer.render(candidate)
        );
    }

    @Test
    void rejectsNullCandidate() {
        assertThrows(
                NullPointerException.class,
                () -> renderer.render(null)
        );
    }

    private static ContextCandidate candidate(Object value) {
        return new ContextCandidate(
                "source",
                new ContextItem<>(
                        "item",
                        value,
                        ContextPriority.NORMAL,
                        ContextAuthority.TRUSTED,
                        Instant.parse("2026-08-21T00:00:00Z"),
                        null,
                        Map.of()
                )
        );
    }

    private record Product(long id, String name) {
    }
}