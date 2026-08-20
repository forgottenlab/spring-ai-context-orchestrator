package io.github.forgottenlab.aicontext.core;

import java.util.concurrent.CompletableFuture;

/**
 * Extension point for any business context source.
 *
 * <p>The core uses CompletableFuture rather than Reactor so providers can stay
 * independent from Spring while still supporting concurrent retrieval.</p>
 */
public interface ContextSource {

    String id();

    default boolean supports(ContextRequest request) {
        return true;
    }

    CompletableFuture<ContextContribution> load(ContextRequest request);
}