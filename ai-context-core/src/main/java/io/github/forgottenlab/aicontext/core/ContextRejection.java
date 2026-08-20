package io.github.forgottenlab.aicontext.core;

import java.util.Objects;

/**
 * Diagnostic record for a context candidate removed during resolution.
 *
 * <p>winner is null for EXPIRED candidates because expiration is not a
 * competition loss to another source.</p>
 */
public record ContextRejection(
        ContextCandidate candidate,
        ContextRejectionReason reason,
        ContextCandidate winner
) {

    public ContextRejection {
        Objects.requireNonNull(candidate, "candidate must not be null");
        Objects.requireNonNull(reason, "reason must not be null");

        if (reason == ContextRejectionReason.SUPERSEDED) {
            Objects.requireNonNull(
                    winner,
                    "winner must not be null for SUPERSEDED rejection"
            );
        }
    }
}