package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextAssemblyTest {

    @Test
    void copiesAndProtectsBlocks() {
        List<ContextBlock> blocks =
                new ArrayList<>();
        blocks.add(block("first"));

        ContextAssembly assembly =
                new ContextAssembly(blocks);

        blocks.clear();

        assertEquals(1, assembly.blocks().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> assembly.blocks().add(block("illegal"))
        );
    }

    @Test
    void nullBlocksBecomeEmptyAssembly() {
        ContextAssembly assembly =
                new ContextAssembly(null);

        assertTrue(assembly.isEmpty());
        assertTrue(assembly.blocks().isEmpty());
    }

    private static ContextBlock block(String name) {
        return new ContextBlock(
                "source",
                name,
                null,
                name,
                ContextPriority.NORMAL,
                ContextAuthority.TRUSTED,
                Instant.parse("2026-08-21T00:00:00Z"),
                Map.of()
        );
    }
}