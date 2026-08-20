package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextRequestTest {

    @Test
    void copiesAndProtectsAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 42L);

        ContextRequest request = new ContextRequest(
                "recommend a product",
                "conversation-1",
                attributes
        );

        attributes.put("userId", 99L);

        assertEquals(42L, request.attributes().get("userId"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> request.attributes().put("illegal", true)
        );
    }

    @Test
    void convertsNullAttributesToEmptyMap() {
        ContextRequest request = new ContextRequest("hello", null, null);

        assertNotNull(request.attributes());
        assertTrue(request.attributes().isEmpty());
        assertNull(request.conversationId());
    }

    @Test
    void rejectsNullUserInput() {
        assertThrows(
                NullPointerException.class,
                () -> new ContextRequest(null, "conversation-1", Map.of())
        );
    }
}