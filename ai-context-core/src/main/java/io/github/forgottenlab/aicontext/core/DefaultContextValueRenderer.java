package io.github.forgottenlab.aicontext.core;

import java.util.Objects;

/**
 * Minimal dependency-free renderer.
 *
 * <p>The default deliberately delegates to the value's string representation.
 * Applications that need stable JSON or domain-specific formatting should
 * provide a custom ContextValueRenderer.</p>
 */
public final class DefaultContextValueRenderer
        implements ContextValueRenderer {

    @Override
    public String render(ContextCandidate candidate) {
        Objects.requireNonNull(candidate, "candidate must not be null");
        return Objects.toString(candidate.item().value());
    }
}