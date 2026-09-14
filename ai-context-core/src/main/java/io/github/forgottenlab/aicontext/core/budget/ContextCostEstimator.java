package io.github.forgottenlab.aicontext.core.budget;

import io.github.forgottenlab.aicontext.core.ContextCandidate;

/**
 * Estimates the candidate cost consumed by a {@link ContextBudgeter}.
 * 估算 {@link ContextBudgeter} 进行选择时一个候选项占用的成本。
 *
 * <p>
 * Core is tokenizer-neutral: an implementation may use deterministic abstract
 * units or a model-specific tokenizer. A cost unit must therefore not be
 * assumed to equal an exact model token unless the implementation explicitly
 * defines it that way.
 * Core 不绑定具体 tokenizer：实现既可以采用确定性的抽象单位，也可以采用特定
 * 模型的 tokenizer。因此，除非实现明确如此定义，否则成本单位不能被视为精确的
 * 模型 token 数。
 * </p>
 *
 * <p>
 * Implementations must return a non-negative value and should be deterministic
 * for the same candidate whenever possible. Estimation supplies budget input;
 * it does not select or resolve candidates.
 * 实现必须返回非负值，并应尽可能保证同一候选项的估算结果具有确定性。
 * 成本估算只为 Budget 提供输入，不负责选择候选项或解决事实冲突。
 * </p>
 */
@FunctionalInterface
public interface ContextCostEstimator {

    /**
     * Estimates the non-negative budget cost of one candidate.
     * 估算一个候选项的非负预算成本。
     *
     * @param candidate candidate to estimate；待估算的候选项
     * @return cost in the units defined by this implementation；以当前实现所定义单位表示的成本
     */
    long estimate(ContextCandidate candidate);
}
