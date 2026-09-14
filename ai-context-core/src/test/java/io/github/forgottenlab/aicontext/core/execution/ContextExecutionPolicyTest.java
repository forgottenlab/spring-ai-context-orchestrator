package io.github.forgottenlab.aicontext.core.execution;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ContextExecutionPolicyTest {

    @Test
    void defaultsToPositiveTimeout() {
        ContextExecutionPolicy policy = ContextExecutionPolicy.defaults();

        assertNotNull(policy.timeout());
        assertTrue(!policy.timeout().isZero() && !policy.timeout().isNegative());
    }

    @Test
    void rejectsNullZeroAndNegativeTimeout() {
        assertThrows(
                NullPointerException.class,
                () -> new ContextExecutionPolicy(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ContextExecutionPolicy(Duration.ZERO)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ContextExecutionPolicy(Duration.ofMillis(-1))
        );
    }
}