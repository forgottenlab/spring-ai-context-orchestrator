package io.github.forgottenlab.aicontext.core.execution;

import io.github.forgottenlab.aicontext.core.ContextContribution;
import io.github.forgottenlab.aicontext.core.ContextRegistry;
import io.github.forgottenlab.aicontext.core.ContextRequest;
import io.github.forgottenlab.aicontext.core.ContextSource;
import io.github.forgottenlab.aicontext.core.planning.ContextPlan;
import io.github.forgottenlab.aicontext.core.planning.ContextPlanDecision;
import io.github.forgottenlab.aicontext.core.planning.ContextPlanEntry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Default context executor.
 *
 * <p>Execution behavior:</p>
 * <ul>
 *     <li>only LOAD plan entries are executed;</li>
 *     <li>source invocation is isolated on the supplied Executor;</li>
 *     <li>sources may complete concurrently;</li>
 *     <li>one source failure does not fail the whole execution report;</li>
 *     <li>result ordering follows plan ordering, not completion ordering.</li>
 * </ul>
 */
public final class DefaultContextExecutor implements ContextExecutor {

    private final ContextRegistry registry;
    private final Executor executor;

    public DefaultContextExecutor(ContextRegistry registry, Executor executor) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    @Override
    public CompletableFuture<ContextExecutionReport> execute(
            ContextRequest request,
            ContextPlan plan
    ) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(plan, "plan must not be null");

        List<CompletableFuture<ContextExecutionResult>> resultFutures = new ArrayList<>();

        for (ContextPlanEntry entry : plan.entries()) {
            if (entry.decision() != ContextPlanDecision.LOAD) {
                continue;
            }

            ContextSource source = registry.find(entry.sourceId())
                    .orElseThrow(() -> new IllegalStateException(
                            "ContextSource not found for planned source id: " + entry.sourceId()
                    ));

            resultFutures.add(executeSource(source, request));
        }

        CompletableFuture<?>[] all =
                resultFutures.toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(all)
                .thenApply(ignored -> new ContextExecutionReport(
                        resultFutures.stream()
                                .map(CompletableFuture::join)
                                .toList()
                ));
    }

    private CompletableFuture<ContextExecutionResult> executeSource(
            ContextSource source,
            ContextRequest request
    ) {
        long startedAt = System.nanoTime();

        ContextExecutionPolicy policy = Objects.requireNonNull(
                source.executionPolicy(),
                "ContextSource.executionPolicy() must not return null: " + source.id()
        );

        CompletableFuture<ContextContribution> invocation =
                CompletableFuture.supplyAsync(() -> source.load(request), executor)
                        .thenCompose(future -> Objects.requireNonNull(
                                future,
                                "ContextSource.load() must not return null: " + source.id()
                        ));

        return invocation
                .orTimeout(policy.timeout().toMillis(), TimeUnit.MILLISECONDS)
                .handle((contribution, throwable) -> {
                    Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

                    if (throwable == null) {
                        return new ContextExecutionSuccess(
                                source.id(),
                                Objects.requireNonNull(
                                        contribution,
                                        "ContextSource contribution must not be null: " + source.id()
                                ),
                                elapsed
                        );
                    }

                    Throwable cause = unwrap(throwable);
                    ContextExecutionFailureKind kind =
                            cause instanceof TimeoutException
                                    ? ContextExecutionFailureKind.TIMEOUT
                                    : ContextExecutionFailureKind.ERROR;

                    return new ContextExecutionFailure(
                            source.id(),
                            kind,
                            cause,
                            elapsed
                    );
                });
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;

        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }

        return current;
    }
}