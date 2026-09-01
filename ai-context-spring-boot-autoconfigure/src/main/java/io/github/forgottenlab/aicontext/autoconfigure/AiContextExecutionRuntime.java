package io.github.forgottenlab.aicontext.autoconfigure;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Package-private lifecycle holder for ACO execution infrastructure.
 *
 * <p>The holder deliberately does not implement Executor or ExecutorService.
 * This prevents ACO's private worker pool from participating in application
 * infrastructure injection by type.</p>
 */
final class AiContextExecutionRuntime
        implements AutoCloseable {

    private final ExecutorService executorService;

    AiContextExecutionRuntime(
            ExecutorService executorService
    ) {
        this.executorService =
                Objects.requireNonNull(
                        executorService,
                        "executorService must not be null"
                );
    }

    Executor executor() {
        return executorService;
    }

    boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public void close() {
        executorService.shutdown();
    }
}