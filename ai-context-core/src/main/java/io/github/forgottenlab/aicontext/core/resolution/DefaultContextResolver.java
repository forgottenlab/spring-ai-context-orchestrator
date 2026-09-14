package io.github.forgottenlab.aicontext.core.resolution;

import io.github.forgottenlab.aicontext.core.ContextCandidate;
import io.github.forgottenlab.aicontext.core.ContextContribution;
import io.github.forgottenlab.aicontext.core.ContextItem;
import io.github.forgottenlab.aicontext.core.ContextKey;
import io.github.forgottenlab.aicontext.core.ContextPriority;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Default deterministic context resolver.
 *
 * <p>Resolution rules:</p>
 * <ol>
 *     <li>expired items are rejected;</li>
 *     <li>unkeyed items do not conflict and are preserved;</li>
 *     <li>items sharing the same ContextKey compete;</li>
 *     <li>higher authority wins;</li>
 *     <li>for equal authority, newer observedAt wins;</li>
 *     <li>remaining ties preserve first-seen order.</li>
 * </ol>
 *
 * <p>ContextPriority is deliberately ignored here. Priority belongs to the
 * later budget stage and must not decide which conflicting fact is true.</p>
 */
public final class DefaultContextResolver implements ContextResolver {

    @Override
    public ContextResolution resolve(
            Collection<ContextContribution> contributions,
            Instant now
    ) {
        Objects.requireNonNull(contributions, "contributions must not be null");
        Objects.requireNonNull(now, "now must not be null");

        List<ContextCandidate> unkeyedSelected = new ArrayList<>();
        Map<ContextKey, ContextCandidate> keyedWinners = new LinkedHashMap<>();
        List<ContextRejection> rejected = new ArrayList<>();

        for (ContextContribution contribution : contributions) {
            Objects.requireNonNull(
                    contribution,
                    "contribution must not be null"
            );

            for (ContextItem<?> item : contribution.items()) {
                Objects.requireNonNull(item, "context item must not be null");

                ContextCandidate candidate =
                        new ContextCandidate(contribution.sourceId(), item);

                if (item.isExpired(now)) {
                    rejected.add(new ContextRejection(
                            candidate,
                            ContextRejectionReason.EXPIRED,
                            null
                    ));
                    continue;
                }

                if (item.key() == null) {
                    unkeyedSelected.add(candidate);
                    continue;
                }

                ContextCandidate currentWinner = keyedWinners.get(item.key());

                if (currentWinner == null) {
                    keyedWinners.put(item.key(), candidate);
                    continue;
                }

                ContextCandidate winner =
                        preferred(currentWinner, candidate);

                ContextCandidate loser =
                        winner == currentWinner ? candidate : currentWinner;

                keyedWinners.put(item.key(), winner);
                rejected.add(new ContextRejection(
                        loser,
                        ContextRejectionReason.SUPERSEDED,
                        winner
                ));
            }
        }

        List<ContextCandidate> selected = new ArrayList<>(
                unkeyedSelected.size() + keyedWinners.size()
        );

        selected.addAll(unkeyedSelected);
        selected.addAll(keyedWinners.values());

        return new ContextResolution(selected, rejected);
    }

    private ContextCandidate preferred(
            ContextCandidate current,
            ContextCandidate challenger
    ) {
        ContextItem<?> currentItem = current.item();
        ContextItem<?> challengerItem = challenger.item();

        int authorityComparison = Integer.compare(
                challengerItem.authority().strength(),
                currentItem.authority().strength()
        );

        if (authorityComparison > 0) {
            return challenger;
        }

        if (authorityComparison < 0) {
            return current;
        }

        Instant currentObservedAt = currentItem.observedAt();
        Instant challengerObservedAt = challengerItem.observedAt();

        if (currentObservedAt == null && challengerObservedAt != null) {
            return challenger;
        }

        if (currentObservedAt != null && challengerObservedAt == null) {
            return current;
        }

        if (currentObservedAt != null
                && challengerObservedAt != null
                && challengerObservedAt.isAfter(currentObservedAt)) {
            return challenger;
        }

        return current;
    }
}