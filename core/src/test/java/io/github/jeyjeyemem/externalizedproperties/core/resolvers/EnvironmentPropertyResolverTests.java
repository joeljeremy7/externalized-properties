package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentPropertyResolverTests {
    @Nested
    class ResolveMethodSingleProperty {
        @Test
        @DisplayName("should resolve property value from environment variables")
        public void test1() {
            EnvironmentPropertyResolver resolver = resolverToTest();
            Optional<String> result = resolver.resolve(
                "PATH"
            );

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                System.getenv("PATH"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when environment variable is not found"
        )
        public void test2() {
            EnvironmentPropertyResolver resolver = resolverToTest();
            Optional<String> result = resolver.resolve(
                "NON_EXISTING_ENVVAR"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    class ResolveMethodMultipleProperties {
        @Test
        @DisplayName("should resolve property values from environment variables")
        public void test1() {
            EnvironmentPropertyResolver resolver = resolverToTest();
            EnvironmentPropertyResolver.Result result = resolver.resolve(
                "PATH",
                "HOME"
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getenv("PATH"), 
                result.findRequiredProperty("PATH")
            );

            assertEquals(
                System.getenv("HOME"), 
                result.findRequiredProperty("HOME")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when environment variable is not found"
        )
        public void test2() {
            EnvironmentPropertyResolver resolver = resolverToTest();
            EnvironmentPropertyResolver.Result result = resolver.resolve(
                "NON_EXISTING_ENVVAR1",
                "NON_EXISTING_ENVVAR2"
            );
            
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("NON_EXISTING_ENVVAR1"));
            assertTrue(result.unresolvedPropertyNames().contains("NON_EXISTING_ENVVAR2"));
        }
    }

    private EnvironmentPropertyResolver resolverToTest() {
        return new EnvironmentPropertyResolver();
    }
}
