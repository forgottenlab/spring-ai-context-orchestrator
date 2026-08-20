package io.github.forgottenlab.aicontext.core;

/**
 * Relative trust level of a context item when multiple sources disagree.
 */
public enum ContextAuthority {
    AUTHORITATIVE,
    TRUSTED,
    USER_PROVIDED,
    UNVERIFIED
}