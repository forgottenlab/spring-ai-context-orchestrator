package io.github.forgottenlab.aicontext.core.execution;

import io.github.forgottenlab.aicontext.core.ContextContribution;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class ContextExecutionReportTest {

    @Test
    void protectsResultsAndSeparatesSuccessesFromFailures() {
        ContextExecutionSuccess success = new ContextExecutionSuccess(
                "products",
                ContextContribution.empty("products"),
                Duration.ofMillis(10)
        );

        ContextExecutionFailure failure = new ContextExecutionFailure(
                "inventory",
                ContextExecutionFailureKind.TIMEOUT,
                new TimeoutException("timeout"),
                Duration.ofMillis(100)
        );

        List<ContextExecutionResult> input = new ArrayList<>();
        input.add(success);
        input.add(failure);

        ContextExecutionReport report = new ContextExecutionReport(input);
        input.clear();

        assertEquals(2, report.results().size());
        assertEquals(List.of(success), report.successes());
        assertEquals(List.of(failure), report.failures());
        assertEquals(
                List.of(success.contribution()),
                report.contributions()
        );
        assertTrue(report.hasFailures());

        assertThrows(
                UnsupportedOperationException.class,
                () -> report.results().add(success)
        );
    }

    @Test
    void nullResultsBecomeEmptyReport() {
        ContextExecutionReport report = new ContextExecutionReport(null);

        assertTrue(report.results().isEmpty());
        assertTrue(report.successes().isEmpty());
        assertTrue(report.failures().isEmpty());
        assertTrue(report.contributions().isEmpty());
        assertFalse(report.hasFailures());
    }
}