package io.github.forgottenlab.aicontext.core;

import java.time.Instant;
import java.util.Collection;

/**
 * Resolves retrieved context into a non-conflicting set of selected candidates.
 */
public interface ContextResolver {

    ContextResolution resolve(
            Collection<ContextContribution> contributions,
            Instant now
    );
}