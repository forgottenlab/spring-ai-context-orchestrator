package io.github.forgottenlab.aicontext.core.resolution;

import io.github.forgottenlab.aicontext.core.budget.ContextBudgeter;
import io.github.forgottenlab.aicontext.core.ContextContribution;
import io.github.forgottenlab.aicontext.core.ContextKey;
import java.time.Instant;
import java.util.Collection;

/**
 * Resolves retrieved context into a trusted, non-conflicting candidate set.
 * 将已加载的上下文解析为可信且无冲突的候选项集合。
 *
 * <p>
 * Resolution owns {@link ContextKey}-based conflicts, Authority, and
 * Freshness or expiration. Authority answers which competing fact should be
 * trusted.
 * Resolution 负责基于 {@link ContextKey} 的冲突、Authority 以及 Freshness 或过期判断；
 * Authority 回答“相互冲突的事实应该信谁”。
 * </p>
 *
 * <p>
 * Priority intentionally does not belong to this stage. It is considered only
 * after factual conflicts have been resolved, by {@link ContextBudgeter}.
 * Priority 有意不属于本阶段；事实冲突解决后，才由 {@link ContextBudgeter} 在预算阶段处理。
 * </p>
 */
public interface ContextResolver {

    ContextResolution resolve(
            Collection<ContextContribution> contributions,
            Instant now
    );
}
