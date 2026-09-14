package io.github.forgottenlab.aicontext.core;

import io.github.forgottenlab.aicontext.core.execution.ContextExecutionPolicy;
import io.github.forgottenlab.aicontext.core.planning.ContextPlanDecision;
import java.util.concurrent.CompletableFuture;

/**
 * Primary application extension point for supplying business context.
 * 应用向 ACO 提供业务上下文的主要扩展点。
 *
 * <p>
 * One source normally owns one category of business context and declares how
 * it participates in planning. Actual data retrieval happens only in
 * {@link #load(ContextRequest)}.
 * 一个 Source 通常负责一类业务上下文，并声明自己如何参与规划；真实的数据加载只在
 * {@link #load(ContextRequest)} 中发生。
 * </p>
 *
 * <p>
 * A source contributes typed context items. It does not arbitrate conflicting
 * facts, select items under budget pressure, or call the model directly.
 * Source 负责贡献类型化的上下文项，不负责冲突仲裁、预算选择，也不直接调用模型。
 * </p>
 *
 * <p>
 * The core uses {@link CompletableFuture} rather than Reactor so providers can
 * stay independent from Spring while still supporting concurrent retrieval.
 * Core 使用 {@link CompletableFuture} 而不是 Reactor，使提供方在支持并发加载的同时
 * 保持对 Spring 的独立性。
 * </p>
 */
public interface ContextSource {

    String id();

    /**
     * Defines the default planning behavior for this source.
     * 定义该 Source 默认如何参与规划。
     */
    default ContextLoadStrategy loadStrategy() {
        return ContextLoadStrategy.ALWAYS;
    }

    /**
     * Defines execution constraints for this source.
     * 定义该 Source 的执行约束。
     */
    default ContextExecutionPolicy executionPolicy() {
        return ContextExecutionPolicy.defaults();
    }

    /**
     * Technical eligibility guard. Returning false always causes the planner
     * to skip the source for the current request.
     * 技术层面的适用性检查；返回 false 时，Planner 必须在当前请求中跳过该 Source。
     */
    default boolean supports(ContextRequest request) {
        return true;
    }

    /**
     * Relevance hook used only when {@link #loadStrategy()} is RELEVANT.
     * 仅当 {@link #loadStrategy()} 为 RELEVANT 时使用的相关性判断入口。
     */
    default boolean isRelevant(ContextRequest request) {
        return true;
    }

    /**
     * Explicit planning hook used only when {@link #loadStrategy()} is CUSTOM.
     * 仅当 {@link #loadStrategy()} 为 CUSTOM 时使用的显式规划入口。
     */
    default ContextPlanDecision customPlan(ContextRequest request) {
        return ContextPlanDecision.LOAD;
    }

    /**
     * Loads this source's business context for the current request.
     * 为当前请求加载该 Source 负责的业务上下文。
     *
     * <p>
     * This is the source lifecycle method that performs real data access. The
     * executor invokes it only after planning has produced a LOAD decision.
     * 这是 Source 生命周期中执行真实数据访问的方法；只有 Planner 产生 LOAD 决策后，
     * Executor 才会调用它。
     * </p>
     */
    CompletableFuture<ContextContribution> load(ContextRequest request);
}
