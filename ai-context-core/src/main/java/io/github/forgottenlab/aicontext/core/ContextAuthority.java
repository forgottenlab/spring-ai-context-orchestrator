package io.github.forgottenlab.aicontext.core;

import io.github.forgottenlab.aicontext.core.resolution.ContextResolver;

/**
 * Trust level used by a {@link ContextResolver} when conflicting facts compete.
 * 多个事实发生冲突时，由 {@link ContextResolver} 用于仲裁的可信等级。
 *
 * <p>
 * Authority answers "which conflicting fact should be trusted?". It does not
 * decide which resolved context survives budget pressure and is intentionally
 * separate from {@link ContextPriority}.
 * Authority 回答“发生冲突时应该相信哪个事实”。它不决定已完成 Resolution 的
 * 上下文在预算压力下保留谁，并与 {@link ContextPriority} 严格分离。
 * </p>
 */
public enum ContextAuthority {
    AUTHORITATIVE(400),
    TRUSTED(300),
    USER_PROVIDED(200),
    UNVERIFIED(100);

    private final int strength;

    ContextAuthority(int strength) {
        this.strength = strength;
    }

    public int strength() {
        return strength;
    }
}
