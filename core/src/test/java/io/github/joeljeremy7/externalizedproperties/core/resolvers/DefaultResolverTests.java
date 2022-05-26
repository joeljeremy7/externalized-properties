package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.TestProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultResolverTests {
    private static final TestProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new TestProxyMethodFactory<>(ProxyInterface.class);

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property value from system properties.")
        void systemPropertyTest1() {
            DefaultResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::javaVersion,
                externalizedProperties(resolver)
            );

            Optional<String> result = resolver.resolve(proxyMethod, "java.version");

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                System.getProperty("java.version"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when system property is not found."
        )
        void systemPropertyTest2() {
            DefaultResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::notFound,
                externalizedProperties(resolver)
            );

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "not.found"
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should resolve property value from environment variables.")
        void environmentVariableTest1() {
            DefaultResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::path,
                externalizedProperties(resolver)
            );

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "path"
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
        void environmentVariableTest2() {
            DefaultResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::notFound,
                externalizedProperties(resolver)
            );

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "not.found"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());
        }
    }

    private static DefaultResolver resolverToTest() {
        return new DefaultResolver();
    }
    
    private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
        return ExternalizedProperties.builder().resolvers(resolvers).build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("java.version")
        String javaVersion();
        
        @ExternalizedProperty("path")
        String path();

        @ExternalizedProperty("not.found")
        String notFound();
    }
}
