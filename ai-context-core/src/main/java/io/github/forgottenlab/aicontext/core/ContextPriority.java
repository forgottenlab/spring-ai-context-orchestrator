package io.github.forgottenlab.aicontext.core;

import io.github.forgottenlab.aicontext.core.budget.ContextBudgeter;

/**
 * Business importance used by a {@link ContextBudgeter} when model-context
 * budget is limited.
 * 模型上下文预算受限时，由 {@link ContextBudgeter} 使用的业务重要性等级。
 *
 * <p>
 * Priority answers "which already-resolved context should be retained first
 * under budget pressure?". It does not express factual trust and must remain
 * separate from {@link ContextAuthority}.
 * Priority 回答“预算不足时，应优先保留哪些已经完成 Resolution 的上下文”。
 * 它不表示事实可信度，必须与 {@link ContextAuthority} 严格分离。
 * </p>
 */
public enum ContextPriority {
    REQUIRED,
    HIGH,
    NORMAL,
    LOW
}
