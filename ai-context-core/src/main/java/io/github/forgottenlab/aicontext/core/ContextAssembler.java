package io.github.forgottenlab.aicontext.core;

/**
 * Converts budget-approved context candidates into structured rendered blocks.
 */
public interface ContextAssembler {

    ContextAssembly assemble(
            ContextBudgetResult budgetResult,
            ContextValueRenderer renderer
    );
}