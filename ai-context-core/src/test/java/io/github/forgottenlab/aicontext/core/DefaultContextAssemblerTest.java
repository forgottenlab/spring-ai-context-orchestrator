package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DefaultContextAssemblerTest {

    private static final Instant NOW =
            Instant.parse("2026-08-21T00:00:00Z");

    private final ContextAssembler assembler =
            new DefaultContextAssembler();

    @Test
    void assemblesOnlyBudgetSelectedCandidates() {
        ContextCandidate selected =
                candidate(
                        "mysql",
                        "price",
                        "499",
                        ContextKey.of("product", "42", "price"),
                        ContextPriority.HIGH,
                        ContextAuthority.AUTHORITATIVE
                );

        ContextCandidate rejected =
                candidate(
                        "vector",
                        "description",
                        "old description",
                        null,
                        ContextPriority.LOW,
                        ContextAuthority.TRUSTED
                );

        ContextBudgetResult budgetResult =
                new ContextBudgetResult(
                        List.of(selected),
                        List.of(
                                new ContextBudgetRejection(
                                        rejected,
                                        10,
                                        ContextBudgetRejectionReason.BUDGET_EXCEEDED
                                )
                        ),
                        5,
                        5
                );

        ContextAssembly assembly =
                assembler.assemble(
                        budgetResult,
                        new DefaultContextValueRenderer()
                );

        assertEquals(1, assembly.blocks().size());
        assertEquals(
                "mysql",
                assembly.blocks().get(0).sourceId()
        );
    }

    @Test
    void preservesSelectedOrder() {
        ContextCandidate first =
                candidate("first", "a", "A", null,
                        ContextPriority.LOW,
                        ContextAuthority.TRUSTED);

        ContextCandidate second =
                candidate("second", "b", "B", null,
                        ContextPriority.REQUIRED,
                        ContextAuthority.AUTHORITATIVE);

        ContextCandidate third =
                candidate("third", "c", "C", null,
                        ContextPriority.HIGH,
                        ContextAuthority.TRUSTED);

        ContextAssembly assembly =
                assembler.assemble(
                        budget(
                                List.of(first, second, third)
                        ),
                        new DefaultContextValueRenderer()
                );

        assertEquals(
                List.of("first", "second", "third"),
                assembly.blocks().stream()
                        .map(ContextBlock::sourceId)
                        .toList()
        );
    }

    @Test
    void preservesContextIdentityAndGovernanceMetadata() {
        ContextKey key =
                ContextKey.of("product", "42", "price");

        ContextCandidate candidate =
                new ContextCandidate(
                        "mysql",
                        new ContextItem<>(
                                "price",
                                499,
                                key,
                                ContextPriority.HIGH,
                                ContextAuthority.AUTHORITATIVE,
                                NOW,
                                null,
                                Map.of("table", "product")
                        )
                );

        ContextAssembly assembly =
                assembler.assemble(
                        budget(List.of(candidate)),
                        new DefaultContextValueRenderer()
                );

        ContextBlock block =
                assembly.blocks().get(0);

        assertEquals("mysql", block.sourceId());
        assertEquals("price", block.name());
        assertEquals(key, block.key());
        assertEquals("499", block.content());
        assertEquals(ContextPriority.HIGH, block.priority());
        assertEquals(
                ContextAuthority.AUTHORITATIVE,
                block.authority()
        );
        assertEquals(NOW, block.observedAt());
        assertEquals(
                "product",
                block.metadata().get("table")
        );
    }

    @Test
    void customRendererCanProvideDomainSpecificRepresentation() {
        ContextCandidate candidate =
                candidate(
                        "mysql",
                        "product",
                        new Product(42L, "Keyboard"),
                        null,
                        ContextPriority.NORMAL,
                        ContextAuthority.AUTHORITATIVE
                );

        ContextValueRenderer jsonLikeRenderer =
                ignored -> """
                        {"id":42,"name":"Keyboard"}""".trim();

        ContextAssembly assembly =
                assembler.assemble(
                        budget(List.of(candidate)),
                        jsonLikeRenderer
                );

        assertEquals(
                "{\"id\":42,\"name\":\"Keyboard\"}",
                assembly.blocks().get(0).content()
        );
    }

    @Test
    void rendererIsCalledExactlyOncePerSelectedCandidate() {
        ContextCandidate first =
                candidate("first", "a", "A", null,
                        ContextPriority.NORMAL,
                        ContextAuthority.TRUSTED);

        ContextCandidate second =
                candidate("second", "b", "B", null,
                        ContextPriority.NORMAL,
                        ContextAuthority.TRUSTED);

        AtomicInteger calls =
                new AtomicInteger();

        ContextValueRenderer renderer = candidate -> {
            calls.incrementAndGet();
            return candidate.item().value().toString();
        };

        assembler.assemble(
                budget(List.of(first, second)),
                renderer
        );

        assertEquals(2, calls.get());
    }

    @Test
    void rejectsNullRendererOutput() {
        ContextCandidate candidate =
                candidate("source", "item", "value", null,
                        ContextPriority.NORMAL,
                        ContextAuthority.TRUSTED);

        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> assembler.assemble(
                                budget(List.of(candidate)),
                                ignored -> null
                        )
                );

        assertTrue(
                exception.getMessage().contains("source")
        );
    }

    @Test
    void emptyBudgetSelectionProducesEmptyAssembly() {
        ContextAssembly assembly =
                assembler.assemble(
                        new ContextBudgetResult(
                                List.of(),
                                List.of(),
                                0,
                                10
                        ),
                        new DefaultContextValueRenderer()
                );

        assertTrue(assembly.isEmpty());
    }

    @Test
    void rejectsNullInputs() {
        ContextBudgetResult budgetResult =
                new ContextBudgetResult(
                        List.of(),
                        List.of(),
                        0,
                        10
                );

        assertThrows(
                NullPointerException.class,
                () -> assembler.assemble(
                        null,
                        new DefaultContextValueRenderer()
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> assembler.assemble(
                        budgetResult,
                        null
                )
        );
    }

    private static ContextBudgetResult budget(
            List<ContextCandidate> selected
    ) {
        return new ContextBudgetResult(
                selected,
                List.of(),
                selected.size(),
                Math.max(1, selected.size())
        );
    }

    private static ContextCandidate candidate(
            String sourceId,
            String name,
            Object value,
            ContextKey key,
            ContextPriority priority,
            ContextAuthority authority
    ) {
        return new ContextCandidate(
                sourceId,
                new ContextItem<>(
                        name,
                        value,
                        key,
                        priority,
                        authority,
                        NOW,
                        null,
                        Map.of()
                )
        );
    }

    private record Product(long id, String name) {
    }
}