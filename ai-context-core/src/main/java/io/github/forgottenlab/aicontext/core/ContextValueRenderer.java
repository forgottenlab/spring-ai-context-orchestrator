package io.github.forgottenlab.aicontext.core;

/**
 * Converts a typed context candidate into model-consumable text.
 *
 * <p>This is the intentional boundary where typed business values may become
 * textual content. Integrations can replace the renderer, for example with a
 * Jackson-based JSON renderer, without changing the core context model.</p>
 */
@FunctionalInterface
public interface ContextValueRenderer {

    String render(ContextCandidate candidate);
}