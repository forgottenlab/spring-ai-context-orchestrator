package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DefaultContextExecutorTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private static final ContextRequest REQUEST =
            new ContextRequest("hello", "conversation-1", Map.of());

    @AfterEach
    void shutdownExecutor() {
        executor.shutdownNow();
    }

    @Test
    void executesOnlyLoadEntries() {
        AtomicInteger loadCalls = new AtomicInteger();

        ContextSource load = source(
                "load",
                Duration.ofSeconds(1),
                request -> {
                    loadCalls.incrementAndGet();
                    return CompletableFuture.completedFuture(
                            ContextContribution.empty("load")
                    );
                }
        );

        ContextSource deferred = source(
                "deferred",
                Duration.ofSeconds(1),
                request -> {
                    fail("deferred source must not execute");
                    return CompletableFuture.completedFuture(
                            ContextContribution.empty("deferred")
                    );
                }
        );

        ContextSource skipped = source(
                "skipped",
                Duration.ofSeconds(1),
                request -> {
                    fail("skipped source must not execute");
                    return CompletableFuture.completedFuture(
                            ContextContribution.empty("skipped")
                    );
                }
        );

        ContextPlan plan = new ContextPlan(List.of(
                new ContextPlanEntry(
                        "load",
                        ContextLoadStrategy.ALWAYS,
                        ContextPlanDecision.LOAD
                ),
                new ContextPlanEntry(
                        "deferred",
                        ContextLoadStrategy.ON_DEMAND,
                        ContextPlanDecision.DEFER
                ),
                new ContextPlanEntry(
                        "skipped",
                        ContextLoadStrategy.RELEVANT,
                        ContextPlanDecision.SKIP
                )
        ));

        ContextExecutionReport report =
                executor(load, deferred, skipped).execute(REQUEST, plan).join();

        assertEquals(1, loadCalls.get());
        assertEquals(1, report.results().size());
        assertEquals("load", report.results().get(0).sourceId());
    }

    @Test
    void successfulSourcesProduceContributions() {
        ContextSource products = source(
                "products",
                Duration.ofSeconds(1),
                request -> CompletableFuture.completedFuture(
                        ContextContribution.empty("products")
                )
        );

        ContextExecutionReport report =
                executor(products).execute(
                        REQUEST,
                        loadPlan("products")
                ).join();

        assertEquals(1, report.successes().size());
        assertTrue(report.failures().isEmpty());
        assertEquals("products", report.contributions().get(0).sourceId());
    }

    @Test
    void oneAsyncFailureDoesNotFailWholeReport() {
        ContextSource success = source(
                "success",
                Duration.ofSeconds(1),
                request -> CompletableFuture.completedFuture(
                        ContextContribution.empty("success")
                )
        );

        ContextSource failure = source(
                "failure",
                Duration.ofSeconds(1),
                request -> CompletableFuture.failedFuture(
                        new IllegalStateException("boom")
                )
        );

        ContextExecutionReport report =
                executor(success, failure).execute(
                        REQUEST,
                        loadPlan("success", "failure")
                ).join();

        assertEquals(1, report.successes().size());
        assertEquals(1, report.failures().size());
        assertEquals(
                ContextExecutionFailureKind.ERROR,
                report.failures().get(0).kind()
        );
        assertInstanceOf(
                IllegalStateException.class,
                report.failures().get(0).cause()
        );
    }

    @Test
    void synchronousSourceFailureIsIsolated() {
        ContextSource failure = source(
                "sync-failure",
                Duration.ofSeconds(1),
                request -> {
                    throw new IllegalArgumentException("sync boom");
                }
        );

        ContextExecutionReport report =
                executor(failure).execute(
                        REQUEST,
                        loadPlan("sync-failure")
                ).join();

        assertEquals(1, report.failures().size());
        assertEquals(
                ContextExecutionFailureKind.ERROR,
                report.failures().get(0).kind()
        );
        assertInstanceOf(
                IllegalArgumentException.class,
                report.failures().get(0).cause()
        );
    }

    @Test
    void timeoutIsReportedWithoutFailingWholeExecution() {
        CompletableFuture<ContextContribution> never =
                new CompletableFuture<>();

        ContextSource slow = source(
                "slow",
                Duration.ofMillis(50),
                request -> never
        );

        ContextExecutionReport report =
                executor(slow).execute(
                        REQUEST,
                        loadPlan("slow")
                ).join();

        assertEquals(1, report.failures().size());
        assertEquals(
                ContextExecutionFailureKind.TIMEOUT,
                report.failures().get(0).kind()
        );
    }

    @Test
    void preservesPlanOrderEvenWhenCompletionOrderDiffers() {
        CompletableFuture<ContextContribution> first =
                new CompletableFuture<>();
        CompletableFuture<ContextContribution> second =
                new CompletableFuture<>();

        ContextSource firstSource = source(
                "first",
                Duration.ofSeconds(1),
                request -> first
        );

        ContextSource secondSource = source(
                "second",
                Duration.ofSeconds(1),
                request -> second
        );

        CompletableFuture<ContextExecutionReport> reportFuture =
                executor(firstSource, secondSource).execute(
                        REQUEST,
                        loadPlan("first", "second")
                );

        second.complete(ContextContribution.empty("second"));
        first.complete(ContextContribution.empty("first"));

        ContextExecutionReport report = reportFuture.join();

        assertEquals(
                List.of("first", "second"),
                report.results().stream()
                        .map(ContextExecutionResult::sourceId)
                        .toList()
        );
    }

    @Test
    void sourceInvocationsCanStartConcurrently() throws Exception {
        CountDownLatch bothStarted = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);

        ContextSource first = source(
                "first",
                Duration.ofSeconds(1),
                request -> blockingContribution("first", bothStarted, release)
        );

        ContextSource second = source(
                "second",
                Duration.ofSeconds(1),
                request -> blockingContribution("second", bothStarted, release)
        );

        CompletableFuture<ContextExecutionReport> reportFuture =
                executor(first, second).execute(
                        REQUEST,
                        loadPlan("first", "second")
                );

        assertTrue(
                bothStarted.await(500, TimeUnit.MILLISECONDS),
                "both source invocations should start before either is released"
        );

        release.countDown();

        ContextExecutionReport report = reportFuture.join();
        assertEquals(2, report.successes().size());
    }

    @Test
    void missingPlannedSourceIsConfigurationError() {
        DefaultContextExecutor contextExecutor =
                executor();

        assertThrows(
                IllegalStateException.class,
                () -> contextExecutor.execute(
                        REQUEST,
                        loadPlan("missing")
                )
        );
    }

    @Test
    void rejectsNullRequestAndPlan() {
        DefaultContextExecutor contextExecutor =
                executor();

        assertThrows(
                NullPointerException.class,
                () -> contextExecutor.execute(
                        null,
                        new ContextPlan(List.of())
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> contextExecutor.execute(
                        REQUEST,
                        null
                )
        );
    }

    private DefaultContextExecutor executor(ContextSource... sources) {
        return new DefaultContextExecutor(
                new ContextRegistry(List.of(sources)),
                executor
        );
    }

    private static ContextPlan loadPlan(String... sourceIds) {
        return new ContextPlan(
                java.util.Arrays.stream(sourceIds)
                        .map(id -> new ContextPlanEntry(
                                id,
                                ContextLoadStrategy.ALWAYS,
                                ContextPlanDecision.LOAD
                        ))
                        .toList()
        );
    }

    private static CompletableFuture<ContextContribution> blockingContribution(
            String sourceId,
            CountDownLatch bothStarted,
            CountDownLatch release
    ) {
        bothStarted.countDown();

        try {
            if (!release.await(1, TimeUnit.SECONDS)) {
                return CompletableFuture.failedFuture(
                        new IllegalStateException("test release timeout")
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(exception);
        }

        return CompletableFuture.completedFuture(
                ContextContribution.empty(sourceId)
        );
    }

    private static ContextSource source(
            String id,
            Duration timeout,
            Loader loader
    ) {
        return new TestSource(id, timeout, loader);
    }

    @FunctionalInterface
    private interface Loader {
        CompletableFuture<ContextContribution> load(ContextRequest request);
    }

    private record TestSource(
            String id,
            Duration timeout,
            Loader loader
    ) implements ContextSource {

        @Override
        public ContextExecutionPolicy executionPolicy() {
            return new ContextExecutionPolicy(timeout);
        }

        @Override
        public CompletableFuture<ContextContribution> load(ContextRequest request) {
            return loader.load(request);
        }
    }
}