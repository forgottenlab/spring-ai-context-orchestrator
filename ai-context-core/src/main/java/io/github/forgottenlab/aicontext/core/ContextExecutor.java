package io.github.forgottenlab.aicontext.core;

import java.util.concurrent.CompletableFuture;

/**
 * Executes context sources selected by a ContextPlan.
 */
public interface ContextExecutor {

    CompletableFuture<ContextExecutionReport> execute(
            ContextRequest request,
            ContextPlan plan
    );
}