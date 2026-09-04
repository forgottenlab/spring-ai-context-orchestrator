package io.github.forgottenlab.aicontext.core;

import java.util.Objects;

/**
 * Logical identity of a business fact that may be reported by multiple sources.
 * 可能由多个 Source 提供的业务事实的逻辑身份。
 *
 * <p>
 * A key consists of {@code namespace}, {@code subject}, and {@code attribute}.
 * The Resolver uses this identity to decide which candidates describe the same
 * fact and may therefore conflict. A ContextKey is neither a database primary
 * key nor a {@link ContextSource} identifier.
 * Key 由 {@code namespace}、{@code subject} 和 {@code attribute} 组成。
 * Resolver 使用这一身份判断哪些候选项描述同一个事实、因而可能发生冲突。
 * ContextKey 既不是数据库主键，也不等同于 {@link ContextSource} ID。
 * </p>
 *
 * <p>Examples / 示例：</p>
 * <ul>
 *     <li>namespace=product, subject=42, attribute=price</li>
 *     <li>namespace=inventory, subject=sku-1001, attribute=available</li>
 *     <li>namespace=user-preference, subject=user-7, attribute=budget</li>
 * </ul>
 *
 * <p>
 * Items without a ContextKey are treated as non-conflicting context and are
 * not collapsed merely because they share the same display name.
 * 没有 ContextKey 的条目被视为互不冲突的上下文；即使显示名称相同，也不会仅凭
 * 名称被合并或淘汰。
 * </p>
 */
public record ContextKey(
        String namespace,
        String subject,
        String attribute
) {

    public ContextKey {
        namespace = requireText(namespace, "namespace");
        subject = requireText(subject, "subject");
        attribute = requireText(attribute, "attribute");
    }

    public static ContextKey of(
            String namespace,
            String subject,
            String attribute
    ) {
        return new ContextKey(namespace, subject, attribute);
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }

        return normalized;
    }
}
