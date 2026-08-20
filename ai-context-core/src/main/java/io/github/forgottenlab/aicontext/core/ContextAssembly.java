package io.github.forgottenlab.aicontext.core;

import java.util.List;

/**
 * Immutable structured context assembly.
 *
 * <p>The core intentionally does not prescribe a final prompt format or a
 * Spring AI Message type. That translation belongs to an integration layer.</p>
 */
public record ContextAssembly(List<ContextBlock> blocks) {

    public ContextAssembly {
        blocks = blocks == null ? List.of() : List.copyOf(blocks);
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }
}