package io.github.forgottenlab.aicontext.core.execution;

import io.github.forgottenlab.aicontext.core.ContextRequest;
import io.github.forgottenlab.aicontext.core.ContextSource;
import io.github.forgottenlab.aicontext.core.planning.ContextPlan;
import java.util.concurrent.CompletableFuture;

/**
 * Executes the context sources selected for loading by a {@link ContextPlan}.
 * 执行 {@link ContextPlan} 中被选中加载的 ContextSource。
 *
 * <p>
 * Only LOAD entries are executed. Implementations own retrieval mechanics,
 * concurrency, execution constraints, and per-source failure isolation, while
 * preserving deterministic result semantics such as plan ordering.
 * 只有 LOAD 条目会被执行。实现负责加载机制、并发、执行约束和单个 Source 的失败隔离，
 * 同时应保持以 plan 顺序为代表的确定性结果语义。
 * </p>
 *
 * <p>
 * The executor reports what loaded or failed; it does not decide which fact is
 * trustworthy and does not apply the model-context budget.
 * Executor 只报告哪些内容加载成功或失败，不判断事实可信度，也不应用模型上下文预算。
 * </p>
 */
public interface ContextExecutor {

    CompletableFuture<ContextExecutionReport> execute(
            ContextRequest request,
            ContextPlan plan
    );
}
