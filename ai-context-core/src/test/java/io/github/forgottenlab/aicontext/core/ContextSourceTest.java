package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class ContextSourceTest {

    @Test
    void supportsRequestsByDefault() {
        ContextSource source = new StubSource("products");

        assertTrue(source.supports(
                new ContextRequest("hello", "conversation-1", Map.of())
        ));
    }

    @Test
    void asyncLoadPreservesContributionIdentityAndData() {
        ContextSource source = new StubSource("products");
        ContextRequest request = new ContextRequest(
                "recommend a product",
                "conversation-1",
                Map.of("userId", 42L)
        );

        ContextContribution contribution = source.load(request).join();

        assertEquals("products", contribution.sourceId());
        assertEquals(1, contribution.items().size());
        assertEquals("recommend a product", contribution.items().get(0).value());
    }

    private record StubSource(String id) implements ContextSource {
        @Override
        public CompletableFuture<ContextContribution> load(ContextRequest request) {
            ContextItem<String> item = new ContextItem<>(
                    "echo",
                    request.userInput(),
                    ContextPriority.NORMAL,
                    ContextAuthority.TRUSTED,
                    Instant.parse("2026-08-21T00:00:00Z"),
                    null,
                    Map.of()
            );

            return CompletableFuture.completedFuture(
                    new ContextContribution(id, List.of(item))
            );
        }
    }
}