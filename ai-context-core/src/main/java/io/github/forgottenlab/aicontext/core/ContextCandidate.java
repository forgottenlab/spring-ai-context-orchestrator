package io.github.forgottenlab.aicontext.core;

import io.github.forgottenlab.aicontext.core.assembly.ContextBlock;
import java.util.Objects;

/**
 * Pipeline candidate formed from a {@link ContextItem} and its Source provenance.
 * 由 {@link ContextItem} 及其 Source provenance 组成的流水线候选表示。
 *
 * <p>
 * During resolution, items from {@link ContextContribution} instances become
 * candidates by retaining the producing {@code sourceId}. The candidate form
 * then carries that provenance through Resolution and Budget processing.
 * 在 Resolution 阶段，来自 {@link ContextContribution} 的 Item 会与其来源
 * {@code sourceId} 结合成为 Candidate，并携带该 provenance 继续经过 Resolution
 * 与 Budget 阶段。
 * </p>
 *
 * <p>
 * A candidate remains provisional: the Resolver or Budgeter may reject it.
 * It is not the final model-facing {@link ContextBlock}.
 * Candidate 仍是临时候选项，可能被 Resolver 或 Budgeter 淘汰；它并不是最终面向
 * 模型的 {@link ContextBlock}。
 * </p>
 */
public record ContextCandidate(
        String sourceId,
        ContextItem<?> item
) {

    public ContextCandidate {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(item, "item must not be null");
    }
}
