package io.github.forgottenlab.aicontext.autoconfigure;

import io.github.forgottenlab.aicontext.core.ContextAssembler;
import io.github.forgottenlab.aicontext.core.ContextAssembly;
import io.github.forgottenlab.aicontext.core.ContextAuthority;
import io.github.forgottenlab.aicontext.core.ContextBudgetPolicy;
import io.github.forgottenlab.aicontext.core.ContextBudgeter;
import io.github.forgottenlab.aicontext.core.ContextCandidate;
import io.github.forgottenlab.aicontext.core.ContextContribution;
import io.github.forgottenlab.aicontext.core.ContextCostEstimator;
import io.github.forgottenlab.aicontext.core.ContextExecutor;
import io.github.forgottenlab.aicontext.core.ContextItem;
import io.github.forgottenlab.aicontext.core.ContextPlanner;
import io.github.forgottenlab.aicontext.core.ContextPriority;
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
import io.github.forgottenlab.aicontext.springai.SpringAiContextAttributes;
import io.github.forgottenlab.aicontext.springai.SpringAiContextRequestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class AiContextAutoConfigurationTest {

    private static final String AUTO_CONFIGURATION_NAME =
            "io.github.forgottenlab.aicontext.autoconfigure.AiContextAutoConfiguration";

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(AiContextAutoConfiguration.class)
                    );

    @Test
    void publishesExactlyOneAutoConfigurationImport() throws Exception {
        Path classes = Path.of(AiContextAutoConfiguration.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
        Path imports = classes.resolve(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        );
        List<String> candidates = Files.readAllLines(imports, StandardCharsets.UTF_8)
                .stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .toList();

        assertThat(candidates).containsExactly(AUTO_CONFIGURATION_NAME);
    }

    @Test
    void createsTheCompleteDefaultRuntimeGraphWithoutExternalInfrastructure() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertDefaultRuntimeGraph(context);

            assertThat(context.getBean(ContextRegistry.class).all()).isEmpty();
            assertThat(context).doesNotHaveBean(DataSource.class);
            assertThat(context.getBeansOfType(Executor.class)).isEmpty();
            assertThat(context).doesNotHaveBean(UnwantedScannedComponent.class);
            assertThat(context.getBeanDefinitionNames())
                    .noneMatch(name -> name.toLowerCase().contains("redis"))
                    .noneMatch(name -> name.toLowerCase().contains("vectorstore"))
                    .noneMatch(name -> name.toLowerCase().contains("chatmodel"));
        });
    }

    @Test
    void executesAnInMemorySourceThroughTheAdvisorBoundary() {
        contextRunner
                .withBean(ContextSource.class, AiContextAutoConfigurationTest::inventorySource)
                .run(context -> {
                    assertThat(context).hasNotFailed();

                    ContextOrchestrationAdvisor advisor =
                            context.getBean(ContextOrchestrationAdvisor.class);

                    ChatClientRequest enriched = advisor.before(
                            request("How many items remain?"),
                            null
                    );

                    String systemText = enriched.prompt()
                            .getSystemMessage()
                            .getText();

                    assertThat(systemText)
                            .contains("<business-context>")
                            .contains("source=\"inventory\"")
                            .contains(">stock=7</context>");

                    assertThat(enriched.context())
                            .containsKeys(
                                    SpringAiContextAttributes.CONTEXT_PLAN,
                                    SpringAiContextAttributes.EXECUTION_REPORT,
                                    SpringAiContextAttributes.RESOLUTION,
                                    SpringAiContextAttributes.BUDGET_RESULT,
                                    SpringAiContextAttributes.ASSEMBLY
                            );

                    ContextAssembly assembly = (ContextAssembly) enriched.context()
                            .get(SpringAiContextAttributes.ASSEMBLY);
                    assertThat(assembly.blocks()).hasSize(1);
                });
    }

    @Test
    void preservesAUserProvidedReplaceableBean() {
        ContextSource source = inventorySource();
        ContextRegistry customRegistry = new ContextRegistry(List.of(source));

        contextRunner
                .withBean(ContextRegistry.class, () -> customRegistry)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ContextRegistry.class);
                    assertThat(context.getBean(ContextRegistry.class))
                            .isSameAs(customRegistry);
                    assertThat(context.getBean(ContextOrchestrationAdvisor.class))
                            .isNotNull();
                });
    }

    @Test
    void disabledPropertyBacksOffTheWholeRuntimeGraph() {
        contextRunner
                .withPropertyValues("forgottenlab.ai.context.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertNoRuntimeGraph(context);
                });
    }

    @Test
    void missingSpringAiBoundaryBacksOffCleanly() {
        contextRunner
                .withClassLoader(
                        new FilteredClassLoader(ContextOrchestrationAdvisor.class)
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertNoRuntimeGraph(context);
                });
    }

    @Test
    void duplicateSourceIdsFailDuringBootstrap() {
        contextRunner
                .withBean("firstSource", ContextSource.class, () -> source("duplicate", "one"))
                .withBean("secondSource", ContextSource.class, () -> source("duplicate", "two"))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalArgumentException.class)
                            .hasRootCauseMessage("Duplicate ContextSource id: duplicate");
                });
    }

    @Test
    void emptySourceSetProducesAnEmptyAssemblyWithoutFailure() {
        contextRunner.run(context -> {
            ContextOrchestrationAdvisor advisor =
                    context.getBean(ContextOrchestrationAdvisor.class);

            ChatClientRequest original = request("hello");
            ChatClientRequest enriched = advisor.before(original, null);

            assertThat(enriched.prompt()).isEqualTo(original.prompt());
            assertThat((ContextAssembly) enriched.context()
                    .get(SpringAiContextAttributes.ASSEMBLY))
                    .matches(ContextAssembly::isEmpty);
        });
    }

    @Test
    void preservesExistingApplicationInfrastructure() {
        Executor applicationExecutor = Runnable::run;

        contextRunner
                .withBean("applicationExecutor", Executor.class, () -> applicationExecutor)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBeansOfType(Executor.class))
                            .containsOnly(Map.entry("applicationExecutor", applicationExecutor));
                    assertDefaultRuntimeGraph(context);
                });
    }

    @Test
    void mapsTheConfiguredBudgetIntoTheRuntimePolicy() {
        contextRunner
                .withPropertyValues(
                        "forgottenlab.ai.context.budget.max-context-tokens=321"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(ContextBudgetPolicy.class).maxCost())
                            .isEqualTo(321);

                    ContextCandidate candidate = candidate("abc");
                    assertThat(context.getBean(ContextCostEstimator.class)
                            .estimate(candidate))
                            .isEqualTo(3);
                });
    }

    private static void assertDefaultRuntimeGraph(
            org.springframework.boot.test.context.assertj.AssertableApplicationContext context
    ) {
        assertThat(context).hasSingleBean(AiContextProperties.class);
        assertThat(context).hasSingleBean(ContextRegistry.class);
        assertThat(context).hasSingleBean(ContextPlanner.class);
        assertThat(context).hasSingleBean(ContextExecutor.class);
        assertThat(context).hasSingleBean(ContextResolver.class);
        assertThat(context).hasSingleBean(ContextBudgeter.class);
        assertThat(context).hasSingleBean(ContextAssembler.class);
        assertThat(context).hasSingleBean(ContextBudgetPolicy.class);
        assertThat(context).hasSingleBean(ContextCostEstimator.class);
        assertThat(context).hasSingleBean(ContextValueRenderer.class);
        assertThat(context).hasSingleBean(SpringAiContextRequestFactory.class);
        assertThat(context).hasSingleBean(ContextAssemblySystemTextRenderer.class);
        assertThat(context).hasSingleBean(ContextOrchestrationAdvisor.class);

        assertThat(context.getBean(ContextPlanner.class))
                .isInstanceOf(DefaultContextPlanner.class);
        assertThat(context.getBean(ContextExecutor.class))
                .isInstanceOf(DefaultContextExecutor.class);
        assertThat(context.getBean(ContextResolver.class))
                .isInstanceOf(DefaultContextResolver.class);
        assertThat(context.getBean(ContextBudgeter.class))
                .isInstanceOf(DefaultContextBudgeter.class);
        assertThat(context.getBean(ContextAssembler.class))
                .isInstanceOf(DefaultContextAssembler.class);
        assertThat(context.getBean(ContextValueRenderer.class))
                .isInstanceOf(DefaultContextValueRenderer.class);
    }

    private static void assertNoRuntimeGraph(
            org.springframework.boot.test.context.assertj.AssertableApplicationContext context
    ) {
        assertThat(context).doesNotHaveBean(ContextRegistry.class);
        assertThat(context).doesNotHaveBean(ContextPlanner.class);
        assertThat(context).doesNotHaveBean(ContextExecutor.class);
        assertThat(context).doesNotHaveBean(ContextResolver.class);
        assertThat(context).doesNotHaveBean(ContextBudgeter.class);
        assertThat(context).doesNotHaveBean(ContextAssembler.class);
        assertThat(context).doesNotHaveBean(ContextOrchestrationAdvisor.class);
    }

    private static ChatClientRequest request(String userText) {
        return new ChatClientRequest(
                new Prompt(new UserMessage(userText)),
                Map.of()
        );
    }

    private static ContextSource inventorySource() {
        return source("inventory", "stock=7");
    }

    private static ContextSource source(String id, String value) {
        return new ContextSource() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public CompletableFuture<ContextContribution> load(
                    io.github.forgottenlab.aicontext.core.ContextRequest request
            ) {
                return CompletableFuture.completedFuture(
                        new ContextContribution(
                                id,
                                List.of(new ContextItem<>(
                                        "inventory",
                                        value,
                                        ContextPriority.REQUIRED,
                                        ContextAuthority.AUTHORITATIVE,
                                        Instant.parse("2026-08-31T00:00:00Z"),
                                        null,
                                        Map.of()
                                ))
                        )
                );
            }
        };
    }

    private static ContextCandidate candidate(String value) {
        return new ContextCandidate(
                "test",
                new ContextItem<>(
                        "value",
                        value,
                        ContextPriority.NORMAL,
                        ContextAuthority.TRUSTED,
                        Instant.parse("2026-08-31T00:00:00Z"),
                        null,
                        Map.of()
                )
        );
    }

    @Component
    static class UnwantedScannedComponent {
    }
}
