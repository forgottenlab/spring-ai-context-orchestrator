package io.github.forgottenlab.aicontext.core;

/**
 * Raised when REQUIRED context alone cannot fit inside the configured budget.
 *
 * <p>REQUIRED context is never silently dropped. Applications must increase
 * the budget, reduce required context, or explicitly redesign priorities.</p>
 */
public final class RequiredContextBudgetExceededException
        extends IllegalStateException {

    private final long requiredCost;
    private final long maxCost;

    public RequiredContextBudgetExceededException(
            long requiredCost,
            long maxCost
    ) {
        super(
                "Required context cost " + requiredCost
                        + " exceeds context budget " + maxCost
        );
        this.requiredCost = requiredCost;
        this.maxCost = maxCost;
    }

    public long requiredCost() {
        return requiredCost;
    }

    public long maxCost() {
        return maxCost;
    }
}