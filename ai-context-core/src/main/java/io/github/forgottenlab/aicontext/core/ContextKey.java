package io.github.forgottenlab.aicontext.core;

import java.util.Objects;

/**
 * Structured identity for a business fact that may conflict across sources.
 *
 * <p>Examples:</p>
 * <ul>
 *     <li>namespace=product, subject=42, attribute=price</li>
 *     <li>namespace=inventory, subject=sku-1001, attribute=available</li>
 *     <li>namespace=user-preference, subject=user-7, attribute=budget</li>
 * </ul>
 *
 * <p>Items without a ContextKey are treated as non-conflicting context and are
 * not collapsed merely because they share the same display name.</p>
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