package io.github.forgottenlab.aicontext.autoconfigure;

import io.github.forgottenlab.aicontext.core.ContextAssembler;
import io.github.forgottenlab.aicontext.core.ContextBudgetPolicy;
import io.github.forgottenlab.aicontext.core.ContextBudgeter;
import io.github.forgottenlab.aicontext.core.ContextCostEstimator;
import io.github.forgottenlab.aicontext.core.ContextExecutor;
import io.github.forgottenlab.aicontext.core.ContextPlanner;
import io.github.forgottenlab.aicontext.core.ContextRegistry;
import io.github.forgottenlab.aicontext.core.ContextResolver;
import io.github.forgottenlab.aicontext.core.ContextSource;
import io.github.forgottenlab.aicontext.core.ContextValueRenderer;
import io.github.forgottenlab.aicontext.core.DefaultContextAssembler;
import io.github.forgottenlab.aicontext.core.DefaultContextBudgeter;
import io.github.forgottenlab.aicontext.core.DefaultContextExecutor;
import io.github.forgottenlab.aicontext.core.DefaultContextPlanner;
import io.github.forgottenlab.aicontext.core.DefaultContextResolver;
import io.github.forgottenlab.aicontext.core.DefaultContextValueRenderer;
import io.github.forgottenlab.aicontext.springai.ContextAssemblySystemTextRenderer;
import io.github.forgottenlab.aicontext.springai.ContextOrchestrationAdvisor;
import io.github.forgottenlab.aicontext.springai.SpringAiContextRequestFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;

@AutoConfiguration
@ConditionalOnClass(ContextOrchestrationAdvisor.class)
@EnableConfigurationProperties(AiContextProperties.class)
@ConditionalOnProperty(
        prefix = "forgottenlab.ai.context",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AiContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ContextRegistry contextRegistry(ObjectProvider<ContextSource> contextSources) {
        return new ContextRegistry(contextSources.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean
    ContextPlanner contextPlanner(ContextRegistry registry) {
        return new DefaultContextPlanner(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    ContextExecutor contextExecutor(ContextRegistry registry) {
        return new DefaultContextExecutor(registry, ForkJoinPool.commonPool());
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
    ContextBudgetPolicy contextBudgetPolicy(AiContextProperties properties) {
        return new ContextBudgetPolicy(properties.getBudget().getMaxContextTokens());
    }

    @Bean
    @ConditionalOnMissingBean
    ContextValueRenderer contextValueRenderer() {
        return new DefaultContextValueRenderer();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextCostEstimator contextCostEstimator(ContextValueRenderer valueRenderer) {
        return candidate -> {
            String rendered = Objects.requireNonNull(
                    valueRenderer.render(candidate),
                    "rendered context value must not be null"
            );
            return rendered.codePointCount(0, rendered.length());
        };
    }

    @Bean
    @ConditionalOnMissingBean
    SpringAiContextRequestFactory springAiContextRequestFactory() {
        return new SpringAiContextRequestFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextAssemblySystemTextRenderer contextAssemblySystemTextRenderer() {
        return new ContextAssemblySystemTextRenderer();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextOrchestrationAdvisor contextOrchestrationAdvisor(
            SpringAiContextRequestFactory requestFactory,
            ContextPlanner planner,
            ContextExecutor executor,
            ContextResolver resolver,
            ContextBudgeter budgeter,
            ContextAssembler assembler,
            ContextBudgetPolicy budgetPolicy,
            ContextCostEstimator costEstimator,
            ContextValueRenderer valueRenderer,
            ContextAssemblySystemTextRenderer systemTextRenderer
    ) {
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
                Clock.systemUTC()
        );
    }
}
