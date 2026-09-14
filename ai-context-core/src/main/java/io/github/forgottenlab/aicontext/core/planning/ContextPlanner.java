package io.github.forgottenlab.aicontext.core.planning;

import io.github.forgottenlab.aicontext.core.budget.ContextBudgeter;
import io.github.forgottenlab.aicontext.core.ContextRequest;
import io.github.forgottenlab.aicontext.core.ContextSource;
import io.github.forgottenlab.aicontext.core.resolution.ContextResolver;

/**
 * Decides which context sources should participate in one model turn.
 * 决定一次模型调用应该让哪些 ContextSource 参与。
 *
 * <p>
 * A planner produces deterministic LOAD, DEFER, or SKIP decisions from the
 * request and source planning metadata. It decides what should be loaded but
 * never invokes {@link ContextSource#load(ContextRequest)}.
 * Planner 根据请求和 Source 的规划元数据生成确定性的 LOAD、DEFER 或 SKIP 决策；
 * 它只决定“应该加载什么”，绝不调用 {@link ContextSource#load(ContextRequest)}。
 * </p>
 *
 * <p>
 * Authority and conflict resolution belong to {@link ContextResolver};
 * Priority and budget pressure belong to {@link ContextBudgeter}.
 * Authority 与冲突仲裁属于 {@link ContextResolver}；Priority 与预算压力属于
 * {@link ContextBudgeter}，均不由本阶段处理。
 * </p>
 */
public interface ContextPlanner {

    ContextPlan plan(ContextRequest request);
}
