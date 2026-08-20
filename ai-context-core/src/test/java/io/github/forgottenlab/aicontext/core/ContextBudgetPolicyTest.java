package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextBudgetPolicyTest {

    @Test
    void acceptsPositiveBudget() {
        ContextBudgetPolicy policy =
                new ContextBudgetPolicy(12_000);

        assertEquals(12_000, policy.maxCost());
    }

    @Test
    void rejectsZeroAndNegativeBudget() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ContextBudgetPolicy(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ContextBudgetPolicy(-1)
        );
    }
}