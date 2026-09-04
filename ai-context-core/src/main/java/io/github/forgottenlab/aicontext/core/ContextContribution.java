package io.github.forgottenlab.aicontext.core;

import java.util.List;
import java.util.Objects;

/**
 * Structured output of one {@link ContextSource#load(ContextRequest)} operation.
 * 一次 {@link ContextSource#load(ContextRequest)} 操作产生的结构化输出。
 *
 * <p>
 * A contribution groups the {@link ContextItem} values supplied by one Source
 * and preserves that Source's identity. Successful execution exposes these
 * contributions to the Resolver, which turns their items into candidates.
 * Contribution 汇集一个 Source 提供的 {@link ContextItem}，并保留该 Source 的身份。
 * 执行成功后，Executor 将这些 Contribution 交给 Resolver，由 Resolver 把其中的
 * Item 转换为候选项。
 * </p>
 *
 * <p>
 * This is still Source-layer output: its items have not yet passed conflict
 * resolution or budget selection, and the contribution is not final Prompt
 * content.
 * 它仍属于 Source 输出层：其中的 Item 尚未通过冲突仲裁或预算选择，Contribution
 * 也不是最终 Prompt 内容。
 * </p>
 */
public record ContextContribution(
        String sourceId,
        List<ContextItem<?>> items
) {

    public ContextContribution {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static ContextContribution empty(String sourceId) {
        return new ContextContribution(sourceId, List.of());
    }
}
