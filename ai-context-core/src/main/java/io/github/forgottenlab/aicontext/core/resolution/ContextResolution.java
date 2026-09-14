package io.github.forgottenlab.aicontext.core.resolution;

import io.github.forgottenlab.aicontext.core.ContextCandidate;
import io.github.forgottenlab.aicontext.core.ContextItem;
import java.util.List;

/**
 * Immutable result of conflict resolution before budget selection.
 * 冲突仲裁完成、预算选择开始之前的不可变结果。
 *
 * <p>
 * The Resolver produces this object and the Budgeter consumes its selected
 * candidates. "Selected" here means that a candidate survived fact-identity,
 * Authority, and Freshness resolution; it does not mean that the candidate has
 * survived the later context budget.
 * Resolver 产生此对象，Budgeter 消费其中 selected 的候选项。此处的“selected”
 * 表示候选项已通过事实身份、Authority 与 Freshness 仲裁，并不表示它一定能通过
 * 后续上下文预算。
 * </p>
 *
 * <p>
 * Rejections are retained as resolution diagnostics. Priority must not decide
 * factual winners at this stage; it belongs to subsequent budget selection.
 * 被淘汰的候选项作为 Resolution diagnostics 保留。Priority 不得在本阶段决定事实
 * 胜负，它属于后续 Budget 选择。
 * </p>
 */
public record ContextResolution(
        List<ContextCandidate> selected,
        List<ContextRejection> rejected
) {

    public ContextResolution {
        selected = selected == null ? List.of() : List.copyOf(selected);
        rejected = rejected == null ? List.of() : List.copyOf(rejected);
    }

    public List<ContextItem<?>> selectedItems() {
        return selected.stream()
                .map(ContextCandidate::item)
                .toList();
    }

    public boolean hasRejections() {
        return !rejected.isEmpty();
    }
}
