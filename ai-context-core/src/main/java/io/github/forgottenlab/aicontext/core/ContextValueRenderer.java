package io.github.forgottenlab.aicontext.core;

/**
 * Converts a typed context candidate into model-consumable text near the model
 * boundary.
 * 在接近模型边界的位置，将类型化上下文候选项转换为模型可消费的文本。
 *
 * <p>
 * Rendering is deliberately deferred so Planner, Executor, Resolver, and
 * Budgeter can work without depending on prematurely stringified business
 * values. Integrations may replace this boundary, for example with stable JSON
 * or domain-specific formatting, without changing the Core context model.
 * 渲染被有意延后，使 Planner、Executor、Resolver 和 Budgeter 无需依赖过早
 * 字符串化的业务值。集成层可以在不改变 Core 上下文模型的前提下替换这一边界，
 * 例如使用稳定 JSON 或领域专用格式。
 * </p>
 *
 * <p>
 * A renderer only defines textual representation. It does not perform budget
 * selection or conflict resolution.
 * Renderer 只定义文本表示，不负责预算选择或事实冲突仲裁。
 * </p>
 */
@FunctionalInterface
public interface ContextValueRenderer {

    /**
     * Renders one candidate without changing its orchestration decisions.
     * 在不改变候选项编排决策的前提下渲染该候选项。
     *
     * @param candidate typed candidate to render；待渲染的类型化候选项
     * @return model-consumable text；模型可消费的文本
     */
    String render(ContextCandidate candidate);
}
