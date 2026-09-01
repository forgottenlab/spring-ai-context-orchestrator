package io.github.forgottenlab.aicontext.autoconfigure;

import io.github.forgottenlab.aicontext.core.ContextCandidate;
import io.github.forgottenlab.aicontext.core.ContextCostEstimator;
import io.github.forgottenlab.aicontext.core.ContextValueRenderer;

import java.util.Objects;

/**
 * Deterministic tokenizer-neutral default cost estimator.
 *
 * <p>One rendered Unicode code point counts as one cost unit. This preserves
 * the established consumer-baseline semantics without pretending to be an
 * exact model tokenizer. Applications can replace this bean with their own
 * ContextCostEstimator when model-specific accounting is required.</p>
 */
final class ApproximateTextContextCostEstimator
        implements ContextCostEstimator {

    private final ContextValueRenderer renderer;

    ApproximateTextContextCostEstimator(
            ContextValueRenderer renderer
    ) {
        this.renderer =
                Objects.requireNonNull(
                        renderer,
                        "renderer must not be null"
                );
    }

    @Override
    public long estimate(
            ContextCandidate candidate
    ) {
        Objects.requireNonNull(
                candidate,
                "candidate must not be null"
        );

        String rendered =
                Objects.requireNonNull(
                        renderer.render(candidate),
                        "renderer must not return null"
                );

        return rendered.codePointCount(
                0,
                rendered.length()
        );
    }
}