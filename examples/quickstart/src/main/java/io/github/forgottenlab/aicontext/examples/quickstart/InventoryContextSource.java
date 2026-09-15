package io.github.forgottenlab.aicontext.examples.quickstart;

import io.github.forgottenlab.aicontext.core.ContextAuthority;
import io.github.forgottenlab.aicontext.core.ContextContribution;
import io.github.forgottenlab.aicontext.core.ContextItem;
import io.github.forgottenlab.aicontext.core.ContextKey;
import io.github.forgottenlab.aicontext.core.ContextPriority;
import io.github.forgottenlab.aicontext.core.ContextRequest;
import io.github.forgottenlab.aicontext.core.ContextSource;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Component
final class InventoryContextSource implements ContextSource {

    private static final Instant OBSERVED_AT =
            Instant.parse("2026-09-14T00:00:00Z");

    private final AtomicInteger loadCount = new AtomicInteger();

    @Override
    public String id() {
        return "inventory";
    }

    @Override
    public boolean supports(ContextRequest request) {
        return true;
    }

    @Override
    public CompletableFuture<ContextContribution> load(
            ContextRequest request
    ) {
        loadCount.incrementAndGet();

        ContextItem<String> stock = new ContextItem<>(
                "remaining-stock",
                "stock=7",
                ContextKey.of(
                        "inventory",
                        "demo-item",
                        "stock"
                ),
                ContextPriority.REQUIRED,
                ContextAuthority.AUTHORITATIVE,
                OBSERVED_AT,
                null,
                Map.of()
        );

        return CompletableFuture.completedFuture(
                new ContextContribution(
                        id(),
                        List.of(stock)
                )
        );
    }

    int loadCount() {
        return loadCount.get();
    }
}
