package io.github.forgottenlab.aicontext.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextKeyTest {

    @Test
    void createsStructuredIdentity() {
        ContextKey key = ContextKey.of("product", "42", "price");

        assertEquals("product", key.namespace());
        assertEquals("42", key.subject());
        assertEquals("price", key.attribute());
    }

    @Test
    void trimsComponents() {
        ContextKey key = ContextKey.of(
                " product ",
                " 42 ",
                " price "
        );

        assertEquals(
                ContextKey.of("product", "42", "price"),
                key
        );
    }

    @Test
    void rejectsNullComponents() {
        assertThrows(
                NullPointerException.class,
                () -> ContextKey.of(null, "42", "price")
        );

        assertThrows(
                NullPointerException.class,
                () -> ContextKey.of("product", null, "price")
        );

        assertThrows(
                NullPointerException.class,
                () -> ContextKey.of("product", "42", null)
        );
    }

    @Test
    void rejectsBlankComponents() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ContextKey.of(" ", "42", "price")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ContextKey.of("product", " ", "price")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ContextKey.of("product", "42", " ")
        );
    }
}