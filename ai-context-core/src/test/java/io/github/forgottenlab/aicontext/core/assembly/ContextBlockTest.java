package io.github.forgottenlab.aicontext.core.assembly;

import io.github.forgottenlab.aicontext.core.ContextAuthority;
import io.github.forgottenlab.aicontext.core.ContextKey;
import io.github.forgottenlab.aicontext.core.ContextPriority;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextBlockTest {

    @Test
    void copiesAndProtectsMetadata() {
        Map<String, Object> metadata =
                new HashMap<>();
        metadata.put("table", "product");

        ContextBlock block = new ContextBlock(
                "mysql",
                "price",
                ContextKey.of("product", "42", "price"),
                "499",
                ContextPriority.HIGH,
                ContextAuthority.AUTHORITATIVE,
                Instant.parse("2026-08-21T00:00:00Z"),
                metadata
        );

        metadata.put("table", "changed");

        assertEquals(
                "product",
                block.metadata().get("table")
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> block.metadata().put("illegal", true)
        );
    }

    @Test
    void allowsNullOptionalIdentityAndTimestamp() {
        ContextBlock block = new ContextBlock(
                "memory",
                "note",
                null,
                "hello",
                ContextPriority.NORMAL,
                ContextAuthority.USER_PROVIDED,
                null,
                null
        );

        assertNull(block.key());
        assertNull(block.observedAt());
        assertTrue(block.metadata().isEmpty());
    }

    @Test
    void rejectsNullRequiredFields() {
        Instant now =
                Instant.parse("2026-08-21T00:00:00Z");

        assertThrows(
                NullPointerException.class,
                () -> new ContextBlock(
                        null,
                        "name",
                        null,
                        "content",
                        ContextPriority.NORMAL,
                        ContextAuthority.TRUSTED,
                        now,
                        Map.of()
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> new ContextBlock(
                        "source",
                        "name",
                        null,
                        null,
                        ContextPriority.NORMAL,
                        ContextAuthority.TRUSTED,
                        now,
                        Map.of()
                )
        );
    }
}