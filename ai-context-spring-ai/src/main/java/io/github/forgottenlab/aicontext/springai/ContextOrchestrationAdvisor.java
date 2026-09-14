package io.github.forgottenlab.aicontext.springai;

import io.github.forgottenlab.aicontext.core.assembly.ContextAssembler;
import io.github.forgottenlab.aicontext.core.assembly.ContextAssembly;
import io.github.forgottenlab.aicontext.core.budget.ContextBudgetPolicy;
import io.github.forgottenlab.aicontext.core.budget.ContextBudgetResult;
import io.github.forgottenlab.aicontext.core.budget.ContextBudgeter;
import io.github.forgottenlab.aicontext.core.budget.ContextCostEstimator;
import io.github.forgottenlab.aicontext.core.execution.ContextExecutionReport;
import io.github.forgottenlab.aicontext.core.execution.ContextExecutor;
import io.github.forgottenlab.aicontext.core.planning.ContextPlan;
import io.github.forgottenlab.aicontext.core.planning.ContextPlanner;
import io.github.forgottenlab.aicontext.core.ContextRequest;
import io.github.forgottenlab.aicontext.core.resolution.ContextResolution;
import io.github.forgottenlab.aicontext.core.resolution.ContextResolver;
import io.github.forgottenlab.aicontext.core.assembly.ContextValueRenderer;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.core.Ordered;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.CompletionException;

/**
 * Spring AI Advisor bridge for the provider-neutral context pipeline.
 *
 * <p>The advisor performs the core pipeline before the model call:</p>
 *
 * <pre>
 * ChatClientRequest
 *   -> ContextRequest
 *   -> Planner
 *   -> Executor
 *   -> Resolver
 *   -> Budgeter
 *   -> Assembler
 *   -> business-context system-message augmentation
 * </pre>
 *
 * <p>Execution diagnostics are retained in ChatClientRequest advisor context,
 * allowing later advisors and ChatClientResponse users to inspect what was
 * planned, loaded, rejected, budgeted, and assembled.</p>
 */
public final class ContextOrchestrationAdvisor
        implements BaseAdvisor {

    /**
     * Intentionally placed before Spring AI's tool-calling advisor default
     * order (HIGHEST_PRECEDENCE + 300), so business context is prepared once
     * outside the recursive tool-call loop.
     */
    public static final int DEFAULT_ORDER =
            Ordered.HIGHEST_PRECEDENCE + 200;

    private final SpringAiContextRequestFactory requestFactory;
    private final ContextPlanner planner;
    private final ContextExecutor executor;
    private final ContextResolver resolver;
    private final ContextBudgeter budgeter;
    private final ContextAssembler assembler;
    private final ContextBudgetPolicy budgetPolicy;
    private final ContextCostEstimator costEstimator;
    private final ContextValueRenderer valueRenderer;
    private final ContextAssemblySystemTextRenderer systemTextRenderer;
    private final Clock clock;
    private final int order;

    public ContextOrchestrationAdvisor(
            SpringAiContextRequestFactory requestFactory,
            ContextPlanner planner,
            ContextExecutor executor,
            ContextResolver resolver,
            ContextBudgeter budgeter,
            ContextAssembler assembler,
            ContextBudgetPolicy budgetPolicy,
            ContextCostEstimator costEstimator,
            ContextValueRenderer valueRenderer,
            ContextAssemblySystemTextRenderer systemTextRenderer,
            Clock clock
    ) {
        this(
                requestFactory,
                planner,
                executor,
                resolver,
                budgeter,
                assembler,
                budgetPolicy,
                costEstimator,
                valueRenderer,
                systemTextRenderer,
                clock,
                DEFAULT_ORDER
        );
    }

    public ContextOrchestrationAdvisor(
            SpringAiContextRequestFactory requestFactory,
            ContextPlanner planner,
            ContextExecutor executor,
            ContextResolver resolver,
            ContextBudgeter budgeter,
            ContextAssembler assembler,
            ContextBudgetPolicy budgetPolicy,
            ContextCostEstimator costEstimator,
            ContextValueRenderer valueRenderer,
            ContextAssemblySystemTextRenderer systemTextRenderer,
            Clock clock,
            int order
    ) {
        this.requestFactory =
                Objects.requireNonNull(requestFactory, "requestFactory must not be null");
        this.planner =
                Objects.requireNonNull(planner, "planner must not be null");
        this.executor =
                Objects.requireNonNull(executor, "executor must not be null");
        this.resolver =
                Objects.requireNonNull(resolver, "resolver must not be null");
        this.budgeter =
                Objects.requireNonNull(budgeter, "budgeter must not be null");
        this.assembler =
                Objects.requireNonNull(assembler, "assembler must not be null");
        this.budgetPolicy =
                Objects.requireNonNull(budgetPolicy, "budgetPolicy must not be null");
        this.costEstimator =
                Objects.requireNonNull(costEstimator, "costEstimator must not be null");
        this.valueRenderer =
                Objects.requireNonNull(valueRenderer, "valueRenderer must not be null");
        this.systemTextRenderer =
                Objects.requireNonNull(systemTextRenderer, "systemTextRenderer must not be null");
        this.clock =
                Objects.requireNonNull(clock, "clock must not be null");
        this.order = order;
    }

    @Override
    public ChatClientRequest before(
            ChatClientRequest chatClientRequest,
            AdvisorChain advisorChain
    ) {
        Objects.requireNonNull(
                chatClientRequest,
                "chatClientRequest must not be null"
        );

        ContextRequest contextRequest =
                requestFactory.create(chatClientRequest);

        ContextPlan plan =
                planner.plan(contextRequest);

        ContextExecutionReport executionReport;

        try {
            executionReport =
                    executor.execute(contextRequest, plan).join();
        } catch (CompletionException exception) {
            throw unwrapCompletionException(exception);
        }

        ContextResolution resolution =
                resolver.resolve(
                        executionReport.contributions(),
                        clock.instant()
                );

        ContextBudgetResult budgetResult =
                budgeter.apply(
                        resolution,
                        budgetPolicy,
                        costEstimator
                );

        ContextAssembly assembly =
                assembler.assemble(
                        budgetResult,
                        valueRenderer
                );

        String businessContextText =
                systemTextRenderer.render(assembly);

        var prompt =
                businessContextText.isEmpty()
                        ? chatClientRequest.prompt()
                        : chatClientRequest.prompt()
                                .augmentSystemMessage(
                                        systemMessage -> systemMessage
                                                .copy()
                                                .mutate()
                                                .text(
                                                        mergeSystemText(
                                                                systemMessage.getText(),
                                                                businessContextText
                                                        )
                                                )
                                                .build()
                                );

        return chatClientRequest.mutate()
                .prompt(prompt)
                .context(
                        SpringAiContextAttributes.CONTEXT_PLAN,
                        plan
                )
                .context(
                        SpringAiContextAttributes.EXECUTION_REPORT,
                        executionReport
                )
                .context(
                        SpringAiContextAttributes.RESOLUTION,
                        resolution
                )
                .context(
                        SpringAiContextAttributes.BUDGET_RESULT,
                        budgetResult
                )
                .context(
                        SpringAiContextAttributes.ASSEMBLY,
                        assembly
                )
                .build();
    }

    @Override
    public ChatClientResponse after(
            ChatClientResponse chatClientResponse,
            AdvisorChain advisorChain
    ) {
        return chatClientResponse;
    }

    @Override
    public String getName() {
        return ContextOrchestrationAdvisor.class.getSimpleName();
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Spring AI 1.1.x Prompt.augmentSystemMessage(String) replaces the existing
     * system-message text. The function overload is therefore used by this
     * advisor so user/application instructions are preserved and business
     * context is appended explicitly.
     */
    private static String mergeSystemText(
            String existingSystemText,
            String businessContextText
    ) {
        Objects.requireNonNull(
                businessContextText,
                "businessContextText must not be null"
        );

        if (existingSystemText == null
                || existingSystemText.isBlank()) {
            return businessContextText;
        }

        return existingSystemText
                + System.lineSeparator()
                + System.lineSeparator()
                + businessContextText;
    }

    private static RuntimeException unwrapCompletionException(
            CompletionException exception
    ) {
        Throwable cause = exception.getCause();

        if (cause instanceof RuntimeException runtimeException) {
            return runtimeException;
        }

        return exception;
    }
}