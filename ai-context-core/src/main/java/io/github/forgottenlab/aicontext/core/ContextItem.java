package io.github.forgottenlab.aicontext.core;

import io.github.forgottenlab.aicontext.core.assembly.ContextValueRenderer;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * A typed business-context value together with its orchestration metadata.
 * 一个类型化业务上下文值及其编排元数据。
 *
 * <p>
 * The value should remain typed until a {@link ContextValueRenderer} converts
 * it near the model boundary. {@link ContextKey} defines fact identity for
 * conflict arbitration, while {@link ContextAuthority} and
 * {@link ContextPriority} remain separate trust and budget dimensions.
 * 在接近模型边界、由 {@link ContextValueRenderer} 转换之前，值应尽量保持类型化。
 * {@link ContextKey} 定义用于冲突仲裁的事实身份；{@link ContextAuthority} 与
 * {@link ContextPriority} 则分别表达可信度和预算优先级，两者严格分离。
 * </p>
 *
 * <p>
 * {@code observedAt} and {@code maxAge} provide freshness facts for resolution.
 * Metadata may be preserved as provenance, but its presence does not guarantee
 * that the item or the metadata will appear in the final model prompt.
 * {@code observedAt} 与 {@code maxAge} 为 Resolution 提供 Freshness 事实。
 * metadata 可以作为 provenance 保留，但它的存在并不保证该候选项或这些元数据
 * 最终一定会进入模型 Prompt。
 * </p>
 *
 * @param name human-readable logical item name；便于人理解的逻辑条目名称
 * @param value typed value whose rendering is intentionally deferred；有意延后渲染的类型化值
 * @param key optional structured fact identity for conflict arbitration；用于冲突仲裁的可选结构化事实身份
 * @param priority budget-survival priority；预算不足时的保留优先级
 * @param authority conflict-resolution trust level；冲突仲裁时的可信等级
 * @param observedAt when the value was observed or loaded；值被观测或加载的时间
 * @param maxAge optional validity window; {@code null} leaves expiration unspecified；可选有效期，{@code null} 表示未指定过期窗口
 * @param metadata source-specific metadata, not guaranteed to be model-facing；Source 特定元数据，不保证面向模型输出
 * @param <T> value type；值类型
 */
public record ContextItem<T>(
        String name,
        T value,
        ContextKey key,
        ContextPriority priority,
        ContextAuthority authority,
        Instant observedAt,
        Duration maxAge,
        Map<String, Object> metadata
) {

    /**
     * Convenience constructor for context that does not participate in
     * key-based fact arbitration.
     * 用于不参与基于 {@link ContextKey} 的事实冲突仲裁的上下文便捷构造器。
     */
    public ContextItem(
            String name,
            T value,
            ContextPriority priority,
            ContextAuthority authority,
            Instant observedAt,
            Duration maxAge,
            Map<String, Object> metadata
    ) {
        this(
                name,
                value,
                null,
                priority,
                authority,
                observedAt,
                maxAge,
                metadata
        );
    }

    public ContextItem {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(priority, "priority must not be null");
        Objects.requireNonNull(authority, "authority must not be null");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    /**
     * Returns whether this item is strictly past its freshness boundary.
     * 判断当前条目是否已严格超过 Freshness 边界。
     *
     * <p>
     * An item is not expired exactly at {@code observedAt + maxAge}; expiration
     * starts after that instant. Missing {@code observedAt} or {@code maxAge}
     * leaves the item unexpired at this Core boundary.
     * 条目在 {@code observedAt + maxAge} 时刻尚未过期，只有超过该时刻才过期。
     * 缺少 {@code observedAt} 或 {@code maxAge} 时，Core 在此边界不判定其过期。
     * </p>
     *
     * @param now resolution time；执行 Resolution 时采用的当前时间
     * @return {@code true} only when the complete freshness window has elapsed；仅在完整有效期已经过去时返回 {@code true}
     */
    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return observedAt != null
                && maxAge != null
                && now.isAfter(observedAt.plus(maxAge));
    }
}
