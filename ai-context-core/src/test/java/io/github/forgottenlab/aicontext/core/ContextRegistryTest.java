package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class ContextRegistryTest {

    @Test
    void resolvesSourcesById() {
        ContextSource products = new StubSource("products");
        ContextRegistry registry = new ContextRegistry(List.of(products));

        assertSame(products, registry.find("products").orElseThrow());
        assertTrue(registry.find("missing").isEmpty());
    }

    @Test
    void preservesRegistrationOrderDeterministically() {
        ContextSource first = new StubSource("first");
        ContextSource second = new StubSource("second");
        ContextSource third = new StubSource("third");

        ContextRegistry registry = new ContextRegistry(List.of(first, second, third));

        assertEquals(
                List.of("first", "second", "third"),
                registry.all().stream().map(ContextSource::id).toList()
        );
    }

    @Test
    void protectsRegistryFromCallerMutation() {
        List<ContextSource> sources = new ArrayList<>();
        sources.add(new StubSource("first"));

        ContextRegistry registry = new ContextRegistry(sources);
        sources.add(new StubSource("second"));

        assertEquals(1, registry.all().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> registry.all().add(new StubSource("illegal"))
        );
    }

    @Test
    void rejectsDuplicateIds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ContextRegistry(List.of(
                        new StubSource("duplicate"),
                        new StubSource("duplicate")
                ))
        );
    }

    @Test
    void rejectsNullAndBlankSourceIds() {
        ContextSource nullId = new StubSource(null);
        ContextSource blankId = new StubSource("   ");

        assertThrows(
                NullPointerException.class,
                () -> new ContextRegistry(List.of(nullId))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ContextRegistry(List.of(blankId))
        );
    }

    @Test
    void rejectsNullSourceElements() {
        List<ContextSource> sources = new ArrayList<>();
        sources.add(null);

        assertThrows(
                NullPointerException.class,
                () -> new ContextRegistry(sources)
        );
    }

    private record StubSource(String id) implements ContextSource {
        @Override
        public CompletableFuture<ContextContribution> load(ContextRequest request) {
            return CompletableFuture.completedFuture(ContextContribution.empty(id));
        }
    }
}