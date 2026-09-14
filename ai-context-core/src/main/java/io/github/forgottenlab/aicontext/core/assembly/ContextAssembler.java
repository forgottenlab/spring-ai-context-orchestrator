package io.github.forgottenlab.aicontext.core.assembly;

import io.github.forgottenlab.aicontext.core.budget.ContextBudgeter;
import io.github.forgottenlab.aicontext.core.budget.ContextBudgetResult;
import io.github.forgottenlab.aicontext.core.resolution.ContextResolver;

/**
 * Converts budget-approved candidates into structured model-facing context.
 * 将预算阶段通过的候选项转换为面向模型的结构化上下文。
 *
 * <p>
 * Typed business values should remain structured until this model-boundary
 * stage, where a {@link ContextValueRenderer} turns them into content. The
 * resulting {@link ContextAssembly} retains the provenance and governance
 * information required by integration layers.
 * 类型化业务值应尽量保持结构，直到靠近模型边界的本阶段，再由
 * {@link ContextValueRenderer} 转换为文本内容。生成的 {@link ContextAssembly}
 * 继续保留集成层所需的来源与治理信息。
 * </p>
 *
 * <p>
 * Assembly does not repeat Authority arbitration or budget selection; those
 * decisions belong to {@link ContextResolver} and {@link ContextBudgeter}.
 * Assembly 不重新执行 Authority 仲裁或预算选择；这些决策分别属于
 * {@link ContextResolver} 和 {@link ContextBudgeter}。
 * </p>
 */
public interface ContextAssembler {

    ContextAssembly assemble(
            ContextBudgetResult budgetResult,
            ContextValueRenderer renderer
    );
}
