package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VariableExpansionExceptionTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should set exception message")
        void test1() {
            VariableExpansionException ex = 
                new VariableExpansionException("My message");
            assertEquals("My message", ex.getMessage());
        }

        @Test
        @DisplayName("should set exception message and cause")
        void test2() {
            Throwable cause = new RuntimeException();
            VariableExpansionException ex = 
                new VariableExpansionException("My message", cause);
            assertEquals("My message", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }
    }
}
