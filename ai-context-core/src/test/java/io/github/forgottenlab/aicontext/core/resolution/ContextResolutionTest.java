package io.github.forgottenlab.aicontext.core.resolution;

import io.github.forgottenlab.aicontext.core.ContextCandidate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextResolutionTest {

    @Test
    void protectsSelectedAndRejectedCollections() {
        List<ContextCandidate> selected = new ArrayList<>();
        List<ContextRejection> rejected = new ArrayList<>();

        ContextResolution resolution =
                new ContextResolution(selected, rejected);

        selected.add(null);
        rejected.add(null);

        assertTrue(resolution.selected().isEmpty());
        assertTrue(resolution.rejected().isEmpty());

        assertThrows(
                UnsupportedOperationException.class,
                () -> resolution.selected().add(null)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> resolution.rejected().add(null)
        );
    }

    @Test
    void nullCollectionsBecomeEmpty() {
        ContextResolution resolution =
                new ContextResolution(null, null);

        assertTrue(resolution.selected().isEmpty());
        assertTrue(resolution.rejected().isEmpty());
        assertTrue(resolution.selectedItems().isEmpty());
        assertFalse(resolution.hasRejections());
    }
}