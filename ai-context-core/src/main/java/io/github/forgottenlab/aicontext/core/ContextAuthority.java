package io.github.forgottenlab.aicontext.core;

/**
 * Relative trust level of a context item when multiple sources disagree.
 *
 * <p>Authority is intentionally separate from priority. Authority answers
 * "which fact should be trusted?", while priority answers "which context
 * should survive when the model context budget is constrained?".</p>
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