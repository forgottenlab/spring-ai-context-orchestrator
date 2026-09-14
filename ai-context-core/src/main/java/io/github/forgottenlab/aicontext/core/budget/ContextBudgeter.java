package io.github.forgottenlab.aicontext.core.budget;

import io.github.forgottenlab.aicontext.core.resolution.ContextResolution;
import io.github.forgottenlab.aicontext.core.resolution.ContextResolver;

/**
 * Selects which already-resolved context survives model-context budget pressure.
 * 决定已经完成 Resolution 的上下文中，哪些内容能在模型上下文预算压力下保留。
 *
 * <p>
 * Input candidates are expected to have completed factual conflict resolution.
 * Required context is protected first; optional context is selected according
 * to Priority and the supplied budget policy. Priority answers which context
 * should be kept when the budget is insufficient.
 * 输入候选项应已完成事实冲突处理。Required context 优先受到保护；optional context
 * 根据 Priority 和给定预算策略进行选择。Priority 回答“预算不足时应该保留谁”。
 * </p>
 *
 * <p>
 * Authority intentionally does not belong to this stage. Trust decisions are
 * completed earlier by {@link ContextResolver}.
 * Authority 有意不属于本阶段；事实可信度应已由前序 {@link ContextResolver} 完成判断。
 * </p>
 */
public interface ContextBudgeter {

    ContextBudgetResult apply(
            ContextResolution resolution,
            ContextBudgetPolicy policy,
            ContextCostEstimator estimator
    );
}
