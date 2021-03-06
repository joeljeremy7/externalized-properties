package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentVariableResolverTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);
    
    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property value from environment variables.")
        void test1() {
            EnvironmentVariableResolver resolver = resolverToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::path,
                externalizedProperties(resolver)
            );

            Optional<String> result = resolver.resolve(
                context,
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
            "should return empty Optional when environment variable is not found."
        )
        void test2() {
            EnvironmentVariableResolver resolver = resolverToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::notFound,
                externalizedProperties(resolver)
            );

            Optional<String> result = resolver.resolve(
                context,
                "not.found"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName(
            "should attempt to resolve environment variable by formatting " + 
            "property name to environment variable format."
        )
        void test3() {
            EnvironmentVariableResolver resolver = resolverToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::javaHome,
                externalizedProperties(resolver)
            );

            Optional<String> result1 = resolver.resolve(
                context,
                // java.home should be converted to JAVA_HOME
                "java.home"
            );

            Optional<String> result2 = resolver.resolve(
                context,
                // java-home should be converted to JAVA_HOME
                "java-home"  
            );

            assertNotNull(result1);
            assertNotNull(result2);
            assertTrue(result1.isPresent());
            assertTrue(result2.isPresent());
            assertEquals(System.getenv("JAVA_HOME"), result1.get());
            assertEquals(System.getenv("JAVA_HOME"), result2.get());
        }
    }

    private static EnvironmentVariableResolver resolverToTest() {
        return new EnvironmentVariableResolver();
    }
    
    private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
        return ExternalizedProperties.builder().resolvers(resolvers).build();
    }

    private static interface ProxyInterface {
        /**
         * EnvironmentVariableResolver supports formatting of
         * property names such that path is converted to PATH.
         */
        @ExternalizedProperty("path")
        String path();

        /**
         * EnvironmentVariableResolver supports formatting of
         * property names such that java.home is converted to JAVA_HOME.
         */
        @ExternalizedProperty("java.home")
        String javaHome();

        @ExternalizedProperty("not.found")
        String notFound();
    }
}
