package io.github.forgottenlab.aicontext.core.assembly;

import io.github.forgottenlab.aicontext.core.budget.ContextBudgetResult;
import io.github.forgottenlab.aicontext.core.ContextCandidate;
import io.github.forgottenlab.aicontext.core.ContextItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default deterministic structured context assembler.
 *
 * <p>The assembler renders selected candidates only, preserves budget-result
 * ordering, and retains provenance/authority metadata in every ContextBlock.</p>
 */
public final class DefaultContextAssembler
        implements ContextAssembler {

    @Override
    public ContextAssembly assemble(
            ContextBudgetResult budgetResult,
            ContextValueRenderer renderer
    ) {
        Objects.requireNonNull(
                budgetResult,
                "budgetResult must not be null"
        );
        Objects.requireNonNull(
                renderer,
                "renderer must not be null"
        );

        List<ContextBlock> blocks = new ArrayList<>();

        for (ContextCandidate candidate : budgetResult.selected()) {
            Objects.requireNonNull(
                    candidate,
                    "selected candidate must not be null"
            );

            String content = Objects.requireNonNull(
                    renderer.render(candidate),
                    "ContextValueRenderer must not return null for source "
                            + candidate.sourceId()
            );

            ContextItem<?> item = candidate.item();

            blocks.add(new ContextBlock(
                    candidate.sourceId(),
                    item.name(),
                    item.key(),
                    content,
                    item.priority(),
                    item.authority(),
                    item.observedAt(),
                    item.metadata()
            ));
        }

        return new ContextAssembly(blocks);
    }
}