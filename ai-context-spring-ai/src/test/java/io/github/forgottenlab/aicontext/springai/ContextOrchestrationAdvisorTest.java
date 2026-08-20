package io.github.forgottenlab.aicontext.springai;

import io.github.forgottenlab.aicontext.core.ContextAssembler;
import io.github.forgottenlab.aicontext.core.ContextAuthority;
import io.github.forgottenlab.aicontext.core.ContextBudgetPolicy;
import io.github.forgottenlab.aicontext.core.ContextBudgeter;
import io.github.forgottenlab.aicontext.core.ContextContribution;
import io.github.forgottenlab.aicontext.core.ContextExecutionFailureKind;
import io.github.forgottenlab.aicontext.core.ContextExecutionReport;
import io.github.forgottenlab.aicontext.core.ContextExecutor;
import io.github.forgottenlab.aicontext.core.ContextItem;
import io.github.forgottenlab.aicontext.core.ContextKey;
import io.github.forgottenlab.aicontext.core.ContextPlanner;
import io.github.forgottenlab.aicontext.core.ContextPriority;
import io.github.forgottenlab.aicontext.core.ContextRegistry;
import io.github.forgottenlab.aicontext.core.ContextResolver;
import io.github.forgottenlab.aicontext.core.ContextSource;
import io.github.forgottenlab.aicontext.core.DefaultContextAssembler;
import io.github.forgottenlab.aicontext.core.DefaultContextBudgeter;
import io.github.forgottenlab.aicontext.core.DefaultContextExecutor;
import io.github.forgottenlab.aicontext.core.DefaultContextPlanner;
import io.github.forgottenlab.aicontext.core.DefaultContextResolver;
import io.github.forgottenlab.aicontext.core.DefaultContextValueRenderer;
import io.github.forgottenlab.aicontext.core.RequiredContextBudgetExceededException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ContextOrchestrationAdvisorTest {

    private static final Instant NOW =
            Instant.parse("2026-08-21T00:00:00Z");

    private final ExecutorService executorService =
            Executors.newFixedThreadPool(4);

    @AfterEach
    void shutdownExecutor() {
        executorService.shutdownNow();
    }

    @Test
    void enrichesSpringAiPromptWithBusinessContext() {
        ContextSource source =
                successSource(
                        "mysql",
                        new ContextItem<>(
                                "price",
                                499,
                                ContextKey.of(
                                        "product",
                                        "42",
                                        "price"
                                ),
                                ContextPriority.HIGH,
                                ContextAuthority.AUTHORITATIVE,
                                NOW,
                                Duration.ofMinutes(1),
                                Map.of()
                        )
                );

        ContextOrchestrationAdvisor advisor =
                advisor(
                        List.of(source),
                        100
                );

        ChatClientRequest enriched =
                advisor.before(
                        request(
                                "How much is product 42?",
                                Map.of()
                        ),
                        null
                );

        assertEquals(
                "How much is product 42?",
                enriched.prompt().getUserMessage().getText()
        );

        String systemText =
                enriched.prompt()
                        .getSystemMessage()
                        .getText();

        assertTrue(systemText.contains("<business-context>"));
        assertTrue(systemText.contains(">499</context>"));
        assertTrue(systemText.contains("source=\"mysql\""));
    }

    @Test
    void augmentsExistingSystemMessageInsteadOfReplacingIt() {
        ContextSource source =
                successSource(
                        "mysql",
                        item(
                                "fact",
                                "business-value",
                                ContextPriority.NORMAL
                        )
                );

        ContextOrchestrationAdvisor advisor =
                advisor(List.of(source), 100);

        Prompt prompt =
                new Prompt(
                        List.of(
                                new SystemMessage(
                                        "You are a helpful assistant."
                                ),
                                new UserMessage("hello")
                        )
                );

        ChatClientRequest enriched =
                advisor.before(
                        new ChatClientRequest(
                                prompt,
                                Map.of()
                        ),
                        null
                );

        String systemText =
                enriched.prompt()
                        .getSystemMessage()
                        .getText();

        assertTrue(
                systemText.contains(
                        "You are a helpful assistant."
                )
        );
        assertTrue(
                systemText.contains(
                        "<business-context>"
                )
        );
    }

    @Test
    void exposesPipelineDiagnosticsThroughAdvisorContext() {
        ContextSource source =
                successSource(
                        "mysql",
                        item(
                                "fact",
                                "value",
                                ContextPriority.NORMAL
                        )
                );

        ContextOrchestrationAdvisor advisor =
                advisor(List.of(source), 100);

        ChatClientRequest enriched =
                advisor.before(
                        request("hello", Map.of()),
                        null
                );

        assertNotNull(
                enriched.context().get(
                        SpringAiContextAttributes.CONTEXT_PLAN
                )
        );
        assertNotNull(
                enriched.context().get(
                        SpringAiContextAttributes.EXECUTION_REPORT
                )
        );
        assertNotNull(
                enriched.context().get(
                        SpringAiContextAttributes.RESOLUTION
                )
        );
        assertNotNull(
                enriched.context().get(
                        SpringAiContextAttributes.BUDGET_RESULT
                )
        );
        assertNotNull(
                enriched.context().get(
                        SpringAiContextAttributes.ASSEMBLY
                )
        );
    }

    @Test
    void partialSourceFailureDoesNotPreventSuccessfulContextInjection() {
        ContextSource success =
                successSource(
                        "success",
                        item(
                                "fact",
                                "available",
                                ContextPriority.NORMAL
                        )
                );

        ContextSource failure =
                new TestSource(
                        "failure",
                        request -> CompletableFuture.failedFuture(
                                new IllegalStateException("boom")
                        )
                );

        ContextOrchestrationAdvisor advisor =
                advisor(
                        List.of(success, failure),
                        100
                );

        ChatClientRequest enriched =
                advisor.before(
                        request("hello", Map.of()),
                        null
                );

        ContextExecutionReport report =
                (ContextExecutionReport)
                        enriched.context().get(
                                SpringAiContextAttributes.EXECUTION_REPORT
                        );

        assertEquals(1, report.successes().size());
        assertEquals(1, report.failures().size());
        assertEquals(
                ContextExecutionFailureKind.ERROR,
                report.failures().get(0).kind()
        );

        assertTrue(
                enriched.prompt()
                        .getSystemMessage()
                        .getText()
                        .contains("available")
        );
    }

    @Test
    void emptyContextLeavesPromptUnmodifiedButStillPublishesDiagnostics() {
        ContextOrchestrationAdvisor advisor =
                advisor(List.of(), 100);

        ChatClientRequest original =
                request("hello", Map.of());

        ChatClientRequest enriched =
                advisor.before(original, null);

        assertEquals(
                original.prompt(),
                enriched.prompt()
        );
        assertNotNull(
                enriched.context().get(
                        SpringAiContextAttributes.ASSEMBLY
                )
        );
    }

    @Test
    void requiredBudgetOverflowPropagatesExplicitly() {
        ContextSource required =
                successSource(
                        "required",
                        item(
                                "must-have",
                                "very-expensive",
                                ContextPriority.REQUIRED
                        )
                );

        ContextOrchestrationAdvisor advisor =
                advisor(
                        List.of(required),
                        1,
                        candidate -> 2
                );

        assertThrows(
                RequiredContextBudgetExceededException.class,
                () -> advisor.before(
                        request("hello", Map.of()),
                        null
                )
        );
    }

    @Test
    void supportsBothCallAndStreamingAdvisorContracts() {
        ContextOrchestrationAdvisor advisor =
                advisor(List.of(), 100);

        assertInstanceOf(CallAdvisor.class, advisor);
        assertInstanceOf(StreamAdvisor.class, advisor);
    }

    @Test
    void exposesConfigurableAdvisorOrder() {
        ContextOrchestrationAdvisor advisor =
                advisor(List.of(), 100);

        assertEquals(
                ContextOrchestrationAdvisor.DEFAULT_ORDER,
                advisor.getOrder()
        );

        ContextOrchestrationAdvisor custom =
                advisor(
                        List.of(),
                        100,
                        candidate -> 1,
                        -1234
                );

        assertEquals(-1234, custom.getOrder());
    }

    private ContextOrchestrationAdvisor advisor(
            List<ContextSource> sources,
            long maxCost
    ) {
        return advisor(
                sources,
                maxCost,
                candidate -> 1
        );
    }

    private ContextOrchestrationAdvisor advisor(
            List<ContextSource> sources,
            long maxCost,
            io.github.forgottenlab.aicontext.core.ContextCostEstimator estimator
    ) {
        return advisor(
                sources,
                maxCost,
                estimator,
                ContextOrchestrationAdvisor.DEFAULT_ORDER
        );
    }

    private ContextOrchestrationAdvisor advisor(
            List<ContextSource> sources,
            long maxCost,
            io.github.forgottenlab.aicontext.core.ContextCostEstimator estimator,
            int order
    ) {
        ContextRegistry registry =
                new ContextRegistry(sources);

        ContextPlanner planner =
                new DefaultContextPlanner(registry);

        ContextExecutor contextExecutor =
                new DefaultContextExecutor(
                        registry,
                        executorService
                );

        ContextResolver resolver =
                new DefaultContextResolver();

        ContextBudgeter budgeter =
                new DefaultContextBudgeter();

        ContextAssembler assembler =
                new DefaultContextAssembler();

        return new ContextOrchestrationAdvisor(
                new SpringAiContextRequestFactory(),
                planner,
                contextExecutor,
                resolver,
                budgeter,
                assembler,
                new ContextBudgetPolicy(maxCost),
                estimator,
                new DefaultContextValueRenderer(),
                new ContextAssemblySystemTextRenderer(),
                Clock.fixed(NOW, ZoneOffset.UTC),
                order
        );
    }

    private static ChatClientRequest request(
            String text,
            Map<String, Object> context
    ) {
        return new ChatClientRequest(
                new Prompt(
                        new UserMessage(text)
                ),
                context
        );
    }

    private static ContextItem<String> item(
            String name,
            String value,
            ContextPriority priority
    ) {
        return new ContextItem<>(
                name,
                value,
                priority,
                ContextAuthority.TRUSTED,
                NOW,
                null,
                Map.of()
        );
    }

    private static ContextSource successSource(
            String id,
            ContextItem<?>... items
    ) {
        return new TestSource(
                id,
                request -> CompletableFuture.completedFuture(
                        new ContextContribution(
                                id,
                                List.of(items)
                        )
                )
        );
    }

    @FunctionalInterface
    private interface Loader {
        CompletableFuture<ContextContribution> load(
                io.github.forgottenlab.aicontext.core.ContextRequest request
        );
    }

    private record TestSource(
            String id,
            Loader loader
    ) implements ContextSource {

        @Override
        public CompletableFuture<ContextContribution> load(
                io.github.forgottenlab.aicontext.core.ContextRequest request
        ) {
            return loader.load(request);
        }
    }
}