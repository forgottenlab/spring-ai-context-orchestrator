package io.github.forgottenlab.aicontext.core;

import java.util.List;

/**
 * Immutable result of applying a model-context budget to resolved candidates.
 * 对已经完成 Resolution 的候选项应用模型上下文预算后得到的不可变结果。
 *
 * <p>
 * The Budgeter produces this object for the Assembler. Its selected candidates
 * survived budget pressure, while its rejections record ordinary budget
 * exclusions. {@code usedCost} and {@code maxCost} retain budget diagnostics in
 * the units defined by the active {@link ContextCostEstimator}.
 * Budgeter 产生此对象并交给 Assembler。selected 候选项已经通过预算约束，rejected
 * 则记录普通的预算淘汰；{@code usedCost} 与 {@code maxCost} 使用当前
 * {@link ContextCostEstimator} 定义的单位保留预算诊断信息。
 * </p>
 *
 * <p>
 * Required-context overflow is an explicit
 * {@link RequiredContextBudgetExceededException}, not a normal rejection in
 * this result. Authority is also not a budget-selection criterion because
 * factual trust was settled during Resolution.
 * Required 上下文溢出会显式抛出 {@link RequiredContextBudgetExceededException}，
 * 而不会作为此结果中的普通 rejection。Authority 也不是预算选择依据，因为事实
 * 可信度已经在 Resolution 阶段确定。
 * </p>
 */
public record ContextBudgetResult(
        List<ContextCandidate> selected,
        List<ContextBudgetRejection> rejected,
        long usedCost,
        long maxCost
) {

    public ContextBudgetResult {
        selected = selected == null ? List.of() : List.copyOf(selected);
        rejected = rejected == null ? List.of() : List.copyOf(rejected);

        if (usedCost < 0) {
            throw new IllegalArgumentException("usedCost must not be negative");
        }

        if (maxCost <= 0) {
            throw new IllegalArgumentException("maxCost must be greater than zero");
        }

        if (usedCost > maxCost) {
            throw new IllegalArgumentException(
                    "usedCost must not exceed maxCost"
            );
        }
    }

    public long remainingCost() {
        return maxCost - usedCost;
    }

    public boolean hasRejections() {
        return !rejected.isEmpty();
    }
}
