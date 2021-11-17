package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesBuilder;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.VariableExpansionException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedPropertyException;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CompositePropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.VariableProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Let ExternalizedPropertiesexternalizedProperties.proxy(Class<?> proxyInterface) 
// create the proxy for these test cases.
public class ExternalizedPropertyInvocationHandlerTests {
    @Nested
    class InvokeMethod {
        @Test
        @DisplayName("should resolve property")
        public void test1() {
            Map<String, String> map = new HashMap<>();
            map.put("property", "test.value.1");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);
            String property = proxyInterface.property();

            assertEquals("test.value.1", property);
        }
        
        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value"
        )
        public void test2() {
            Map<String, String> map = new HashMap<>();
            map.put("property.with.default.value", "test.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);
            String property = proxyInterface.propertyWithDefaultValue();

            assertEquals("test.value", property);
        }

        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value parameter"
        )
        public void test3() {
            Map<String, String> map = new HashMap<>();
            map.put("property.with.default.value", "test.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            String providedDefaultValue = "provided.default.value";
            String property = proxyInterface.propertyWithDefaultValueParameter(providedDefaultValue);

            assertEquals("test.value", property);
        }

        @Test
        @DisplayName("should resolve default value from default interface method")
        public void test4() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);
            String property = proxyInterface.propertyWithDefaultValue();

            assertEquals("default.value", property);
        }

        @Test
        @DisplayName("should resolve default value from default interface method parameter")
        public void test5() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            String providedDefaultValue = "provided.default.value";
            String property = proxyInterface.propertyWithDefaultValueParameter(providedDefaultValue);

            assertEquals(providedDefaultValue, property);
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method when not annotated"
        )
        public void test6() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            String property = proxyInterface.propertyWithNoAnnotationButWithDefaultValue();

            assertEquals("default.value", property);
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method parameter " + 
            "when not annotated"
        )
        public void test7() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            String providedDefaultValue = "provided.default.value";
            String property = proxyInterface.propertyWithNoAnnotationButWithDefaultValueParameter(
                providedDefaultValue
            );

            assertEquals(providedDefaultValue, property);
        }

        @Test
        @DisplayName("should throw when an annotated non-Optional property cannot be resolved.")
        public void test8() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            assertThrows(UnresolvedPropertyException.class, () -> {
                proxyInterface.property();
            });
        }

        @Test
        @DisplayName("should throw when an unannotated non-Optional property cannot be resolved.")
        public void test9() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            assertThrows(UnresolvedPropertyException.class, () -> {
                proxyInterface.propertyWithNoAnnotationAndNoDefaultValue();
            });
        }

        @Test
        @DisplayName("should convert a non-String property via Converter.")
        public void test10() {
            Map<String, String> map = new HashMap<>();
            map.put("property.integer.wrapper", "1");
            map.put("property.integer.primitive", "2");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            PrimitiveProxyInterface proxyInterface = 
                externalizedProperties.proxy(PrimitiveProxyInterface.class);

            // Support for wrapper types.
            Integer property = proxyInterface.integerWrapperProperty();
            // Support for primitive types.
            int intProperty = proxyInterface.intPrimitiveProperty();

            assertEquals(1, property);
            assertEquals(2, intProperty);
        }

        /**
         * Variable expansion tests.
         */

        @Test
        @DisplayName("should expand variable in property name.")
        public void testVariableExpansion1() {
            Map<String, String> map = new HashMap<>();
            String customVariableValue = "custom-variable";
            map.put("custom.variable", customVariableValue);
            map.put("property-" + customVariableValue, "property.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            VariableProxyInterface proxyInterface = 
                externalizedProperties.proxy(VariableProxyInterface.class);
            String variableProperty = proxyInterface.variableProperty();

            assertEquals("property.value", variableProperty);
        }

        @Test
        @DisplayName("should throw when variable value cannot be resolved.")
        public void testVariableExpansion2() {
            Map<String, String> map = new HashMap<>();
            map.put("property-custom-variable", "property.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            VariableProxyInterface proxyInterface = 
                externalizedProperties.proxy(VariableProxyInterface.class);
            
            // There is no custom-variable-value property.
            // Property name of VariableProxyInterface.variableProperty() won't be able to be expanded.
            assertThrows(VariableExpansionException.class, 
                () -> proxyInterface.variableProperty()
            );
        }

        /**
         * Optional property test cases.
         */

        @Test
        @DisplayName("should resolve property")
        public void testOptional1() {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional", "test.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            OptionalProxyInterface proxyInterface = 
                externalizedProperties.proxy(OptionalProxyInterface.class);
            Optional<String> property = proxyInterface.optionalProperty();

            assertTrue(property.isPresent());
            assertEquals("test.value", property.get());
        }

        @Test
        @DisplayName("should resolve property from map and not from default interface method value")
        public void testOptional2() {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional.with.default.value", "test.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            OptionalProxyInterface proxyInterface = 
                externalizedProperties.proxy(OptionalProxyInterface.class);
            Optional<String> property = proxyInterface.optionalPropertyWithDefaultValue();

            assertTrue(property.isPresent());
            assertEquals("test.value", property.get());
        }

        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value parameter"
        )
        public void testOptional3() {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional.with.default.value", "test.value");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            OptionalProxyInterface proxyInterface = 
                externalizedProperties.proxy(OptionalProxyInterface.class);

            String providedDefaultValue = "provided.default.value";
            Optional<String> property = 
                proxyInterface.optionalPropertyWithDefaultValueParameter(providedDefaultValue);

            assertTrue(property.isPresent());
            assertEquals("test.value", property.get());
        }

        @Test
        @DisplayName("should resolve default value from default interface method")
        public void testOptional4() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            OptionalProxyInterface proxyInterface = 
                externalizedProperties.proxy(OptionalProxyInterface.class);
            Optional<String> property = proxyInterface.optionalPropertyWithDefaultValue();

            assertTrue(property.isPresent());
            assertEquals("default.value", property.get());
        }

        @Test
        @DisplayName("should resolve default value from default interface method parameter")
        public void testOptional5() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            OptionalProxyInterface proxyInterface = 
                externalizedProperties.proxy(OptionalProxyInterface.class);

            String providedDefaultValue = "provided.default.value";
            Optional<String> property = 
                proxyInterface.optionalPropertyWithDefaultValueParameter(providedDefaultValue);

            assertTrue(property.isPresent());
            assertEquals(providedDefaultValue, property.get());
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method when not annotated"
        )
        public void testOptional6() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            OptionalProxyInterface proxyInterface = 
                externalizedProperties.proxy(OptionalProxyInterface.class);

            Optional<String> property = 
                proxyInterface.optionalPropertyWithNoAnnotationAndWithDefaultValue();

            assertTrue(property.isPresent());
            assertEquals("default.value", property.get());
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method parameter " + 
            "when not annotated"
        )
        public void testOptional7() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            OptionalProxyInterface proxyInterface = 
                externalizedProperties.proxy(OptionalProxyInterface.class);

            String providedDefaultValue = "provided.default.value";
            Optional<String> property = 
                proxyInterface.optionalPropertyWithNoAnnotationAndWithDefaultValueParameter(
                    providedDefaultValue
                );

            assertTrue(property.isPresent());
            assertEquals(providedDefaultValue, property.get());
        }

        @Test
        @DisplayName(
            "should return empty Optional when an annotated Optional property cannot be resolved."
        )
        public void testOptional8() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            OptionalProxyInterface proxyInterface = 
                externalizedProperties.proxy(OptionalProxyInterface.class);

            Optional<String> property = proxyInterface.optionalProperty();

            assertFalse(property.isPresent());
        }

        @Test
        @DisplayName(
            "should return empty Optional when an unannotated Optional property cannot be resolved."
        )
        public void testOptional9() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            OptionalProxyInterface proxyInterface = 
                externalizedProperties.proxy(OptionalProxyInterface.class);

            Optional<String> property = proxyInterface.optionalPropertyWithNoAnnotationAndNoDefaultValue();
            
            assertFalse(property.isPresent());
        }

        @Test
        @DisplayName("should convert a non-String Optional property via Converter.")
        public void testOptional10() {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional.nonstring", "1");

            ExternalizedProperties externalizedProperties = externalizedProperties(map);

            OptionalProxyInterface proxyInterface = 
                externalizedProperties.proxy(OptionalProxyInterface.class);
            Optional<Integer> property = proxyInterface.nonStringOptionalProperty();

            assertTrue(property.isPresent());
            assertEquals(1, property.get());
        }

        @Test
        @DisplayName("should return true when object references are the same")
        public void proxyEqualsMethod1() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            assertTrue(proxyInterface.equals(proxyInterface));
        }

        @Test
        @DisplayName("should return false when object references are not same")
        public void proxyEqualsMethod2() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface1 = 
                externalizedProperties.proxy(BasicProxyInterface.class);
            BasicProxyInterface proxyInterface2 = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            assertFalse(proxyInterface1.equals(proxyInterface2));
        }

        @Test
        @DisplayName("should return proxy's identity hash code")
        public void proxyHashCodeMethod() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            assertEquals(
                System.identityHashCode(proxyInterface),
                proxyInterface.hashCode()
            );
        }

        @Test
        @DisplayName("should return standard Object.toString() format")
        public void proxyToStringMethod() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            assertEquals(
                standardToStringFormat(proxyInterface),
                proxyInterface.toString()
            );
        }

        // Matches implementation of Object.toString()
        private String standardToStringFormat(BasicProxyInterface proxyInterface) {
            return proxyInterface.getClass().getName() + "@" + 
                Integer.toHexString(proxyInterface.hashCode());
        }
    }

    private ExternalizedProperties externalizedProperties(
            Map<String, String> propertySource,
            ConversionHandler<?>... conversionHandlers
    ) {
        return externalizedProperties(
            Arrays.asList(new MapPropertyResolver(propertySource)),
            Arrays.asList(conversionHandlers)
        );
    }

    private ExternalizedProperties externalizedProperties(
            Collection<ExternalizedPropertyResolver> resolvers,
            Collection<ConversionHandler<?>> conversionHandlers
    ) {
        ExternalizedPropertyResolver resolver = CompositePropertyResolver.flatten(resolvers);
        
        ExternalizedPropertiesBuilder builder = 
            ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(resolver)
                .conversionHandlers(conversionHandlers)
                .withCaching(Duration.ofMinutes(5));

        if (conversionHandlers.size() == 0) {
            builder.withDefaultConversionHandlers();
        }

        return builder.build();
    }
}
