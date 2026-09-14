package io.github.forgottenlab.aicontext.autoconfigure;

import io.github.forgottenlab.aicontext.core.assembly.ContextAssembler;
import io.github.forgottenlab.aicontext.core.budget.ContextBudgetPolicy;
import io.github.forgottenlab.aicontext.core.budget.ContextBudgeter;
import io.github.forgottenlab.aicontext.core.budget.ContextCostEstimator;
import io.github.forgottenlab.aicontext.core.execution.ContextExecutor;
import io.github.forgottenlab.aicontext.core.planning.ContextPlanner;
import io.github.forgottenlab.aicontext.core.ContextRegistry;
import io.github.forgottenlab.aicontext.core.resolution.ContextResolver;
import io.github.forgottenlab.aicontext.core.ContextSource;
import io.github.forgottenlab.aicontext.core.assembly.ContextValueRenderer;
import io.github.forgottenlab.aicontext.core.assembly.DefaultContextAssembler;
import io.github.forgottenlab.aicontext.core.budget.DefaultContextBudgeter;
import io.github.forgottenlab.aicontext.core.execution.DefaultContextExecutor;
import io.github.forgottenlab.aicontext.core.planning.DefaultContextPlanner;
import io.github.forgottenlab.aicontext.core.resolution.DefaultContextResolver;
import io.github.forgottenlab.aicontext.core.assembly.DefaultContextValueRenderer;
import io.github.forgottenlab.aicontext.springai.ContextAssemblySystemTextRenderer;
import io.github.forgottenlab.aicontext.springai.ContextOrchestrationAdvisor;
import io.github.forgottenlab.aicontext.springai.SpringAiContextRequestFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Boot consumer baseline for AI Context Orchestrator.
 */
@AutoConfiguration
@ConditionalOnClass({
        ChatClient.class,
        ChatClientCustomizer.class,
        ContextOrchestrationAdvisor.class
})
@EnableConfigurationProperties(
        AiContextProperties.class
)
@ConditionalOnProperty(
        prefix = "forgottenlab.ai.context",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AiContextAutoConfiguration {

    private static final int DEFAULT_PARALLELISM = 4;

    @Bean
    @ConditionalOnMissingBean
    ContextRegistry contextRegistry(
            ObjectProvider<ContextSource> sources
    ) {
        return new ContextRegistry(
                sources.orderedStream().toList()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    ContextPlanner contextPlanner(
            ContextRegistry registry
    ) {
        return new DefaultContextPlanner(
                registry
        );
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    AiContextExecutionRuntime
            aiContextExecutionRuntime() {
        AtomicInteger threadNumber =
                new AtomicInteger();

        ThreadFactory threadFactory =
                runnable -> {
                    Thread thread = new Thread(
                            runnable,
                            "ai-context-"
                                    + threadNumber.incrementAndGet()
                    );
                    thread.setDaemon(true);
                    return thread;
                };

        ExecutorService executorService =
                Executors.newFixedThreadPool(
                        DEFAULT_PARALLELISM,
                        threadFactory
                );

        return new AiContextExecutionRuntime(
                executorService
        );
    }

    @Bean
    @ConditionalOnMissingBean
    ContextExecutor contextExecutor(
            ContextRegistry registry,
            AiContextExecutionRuntime runtime
    ) {
        return new DefaultContextExecutor(
                registry,
                runtime.executor()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    ContextResolver contextResolver() {
        return new DefaultContextResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextBudgeter contextBudgeter() {
        return new DefaultContextBudgeter();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextAssembler contextAssembler() {
        return new DefaultContextAssembler();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextValueRenderer contextValueRenderer() {
        return new DefaultContextValueRenderer();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextCostEstimator contextCostEstimator(
            ContextValueRenderer renderer
    ) {
        return new ApproximateTextContextCostEstimator(
                renderer
        );
    }

    @Bean
    @ConditionalOnMissingBean
    ContextBudgetPolicy contextBudgetPolicy(
            AiContextProperties properties
    ) {
        return new ContextBudgetPolicy(
                properties.getBudget().getMaxContextTokens()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    SpringAiContextRequestFactory
            springAiContextRequestFactory() {
        return new SpringAiContextRequestFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextAssemblySystemTextRenderer
            contextAssemblySystemTextRenderer() {
        return new ContextAssemblySystemTextRenderer();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextOrchestrationAdvisor
            contextOrchestrationAdvisor(
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
                    ObjectProvider<Clock> clocks
            ) {
        Clock clock =
                clocks.getIfUnique(
                        Clock::systemUTC
                );

        return new ContextOrchestrationAdvisor(
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
                clock
        );
    }

    @Bean
    @ConditionalOnMissingBean(
            AiContextChatClientCustomizer.class
    )
    AiContextChatClientCustomizer
            aiContextChatClientCustomizer(
                    ContextOrchestrationAdvisor advisor
            ) {
        return new AiContextChatClientCustomizer(
                advisor
        );
    }
}