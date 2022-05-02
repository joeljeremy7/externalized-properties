package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.UnresolvedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.OptionalConverter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.PrimitiveConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.NoOpConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.MapResolver;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodReference;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.VariableExpansionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Let ExternalizedProperties.proxy(Class<?> proxyInterface) 
// create the proxy for these test cases.
public class ExternalizedPropertiesInvocationHandlerTests {
    @Nested
    class InvokeMethod {
        @Test
        @DisplayName("should resolve property")
        public void test1() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property", "test.value.1");

            ResolverProvider<?> resolverProvider = MapResolver.provider(map);
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::property
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertEquals("test.value.1", result);
        }
        
        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value"
        )
        public void test2() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.with.default.value", "test.value");

            ResolverProvider<?> resolverProvider = MapResolver.provider(map);
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertEquals("test.value", result);
        }

        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value parameter"
        )
        public void test3() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.with.default.value", "test.value");

            ResolverProvider<?> resolverProvider = MapResolver.provider(map);
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface),
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertEquals("test.value", result);
        }

        @Test
        @DisplayName("should resolve default value from default interface method")
        public void test4() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertEquals("default.value", result);
        }

        @Test
        @DisplayName("should resolve default value from default interface method parameter")
        public void test5() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            // Pass default value.
            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertEquals(providedDefaultValue, result);
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method when not annotated"
        )
        public void test6() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationButWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertEquals("default.value", result);
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method parameter " + 
            "when not annotated"
        )
        public void test7() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationButWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            // Pass default value.
            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertEquals(providedDefaultValue, result);
        }

        @Test
        @DisplayName("should throw when an annotated non-Optional property cannot be resolved.")
        public void test8() {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::property
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            assertThrows(UnresolvedPropertiesException.class, () -> {
                handler.invoke(
                    externalizedProperties.proxy(proxyInterface), 
                    proxyMethod, 
                    null
                );
            });
        }

        @Test
        @DisplayName("should throw when an unannotated non-Optional property cannot be resolved.")
        public void test9() {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::propertyWithNoAnnotationAndNoDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            assertThrows(UnresolvedPropertiesException.class, () -> {
                handler.invoke(
                    externalizedProperties.proxy(proxyInterface), 
                    proxyMethod, 
                    null
                );
            });
        }

        /**
         * Conversion tests.
         */

        @Test
        @DisplayName("should convert a non-String property via Converter.")
        public void conversionTest1() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.int", "1");

            ResolverProvider<?> resolverProvider = MapResolver.provider(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> new PrimitiveConverter();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::intProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object intResult = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod,
                null
            );

            assertEquals(1, (int)intResult);
        }

        /**
         * Variable expansion tests.
         */

        @Test
        @DisplayName("should expand variable in property name.")
        public void variableExpansionTest1() throws Throwable {
            Map<String, String> map = new HashMap<>();
            String customVariableValue = "custom-variable";
            map.put("custom.variable", customVariableValue);
            map.put("property-" + customVariableValue, "property.value");

            ResolverProvider<?> resolverProvider = MapResolver.provider(map);
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::variableProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertEquals("property.value", result);
        }

        @Test
        @DisplayName("should throw when variable value cannot be resolved.")
        public void variableExpansionTest2() {
            Map<String, String> map = new HashMap<>();
            map.put("property-custom-variable", "property.value");

            ResolverProvider<?> resolverProvider = MapResolver.provider(map);
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::variableProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            // There is no custom-variable-value property.
            // Property name of VariableProxyInterface.variableProperty() won't be able to be expanded.
            assertThrows(
                VariableExpansionException.class, 
                () -> handler.invoke(
                    externalizedProperties.proxy(proxyInterface), 
                    proxyMethod, 
                    null
                )
            );
        }

        /**
         * Optional property tests.
         */

        @Test
        @DisplayName("should resolve property")
        public void optionalTest1() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional", "test.value");

            ResolverProvider<?> resolverProvider = MapResolver.provider(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> new OptionalConverter(rc);
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals("test.value", optional.get());
        }

        @Test
        @DisplayName("should resolve property from map and not from default interface method value")
        public void optionalTest2() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional.with.default.value", "test.value");

            ResolverProvider<?> resolverProvider = MapResolver.provider(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> new OptionalConverter(rc);
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );
            
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalPropertyWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals("test.value", optional.get());
        }

        @Test
        @DisplayName(
            "should resolve property from map and not from default interface method value parameter"
        )
        public void optionalTest3() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional.with.default.value", "test.value");

            ResolverProvider<?> resolverProvider = MapResolver.provider(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> new OptionalConverter(rc);
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );
            
            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalPropertyWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals("test.value", optional.get());
        }

        @Test
        @DisplayName("should resolve default value from default interface method")
        public void optionalTest4() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalPropertyWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals("default.value", optional.get());
        }

        @Test
        @DisplayName("should resolve default value from default interface method parameter")
        public void optionalTest5() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalPropertyWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals(providedDefaultValue, optional.get());
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method when not annotated"
        )
        public void optionalTest6() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalPropertyWithNoAnnotationAndWithDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals("default.value", optional.get());
        }

        @Test
        @DisplayName(
            "should always return default value from default interface method parameter " + 
            "when not annotated"
        )
        public void optionalTest7() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalPropertyWithNoAnnotationAndWithDefaultValueParameter
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            String providedDefaultValue = "provided.default.value";
            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                new Object[] { providedDefaultValue }
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertEquals(providedDefaultValue, optional.get());
        }

        @Test
        @DisplayName(
            "should return empty Optional when an annotated Optional property cannot be resolved."
        )
        public void optionalTest8() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertFalse(optional.isPresent());
        }

        @Test
        @DisplayName(
            "should return empty Optional when an unannotated Optional property cannot be resolved."
        )
        public void optionalTest9() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::optionalPropertyWithNoAnnotationAndNoDefaultValue
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertFalse(optional.isPresent());
        }

        @Test
        @DisplayName("should convert a non-String Optional property via Converter.")
        public void optionalTest10() throws Throwable {
            Map<String, String> map = new HashMap<>();
            map.put("property.optional.nonstring", "1");

            ResolverProvider<?> resolverProvider = MapResolver.provider(map);
            ConverterProvider<?> converterProvider = (ep, rc) -> new DefaultConverter(rc);
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                ProxyInterface::nonStringOptionalProperty
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object result = handler.invoke(
                externalizedProperties.proxy(proxyInterface), 
                proxyMethod, 
                null
            );

            assertTrue(result instanceof Optional<?>);

            Optional<?> optional = (Optional<?>)result;

            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof Integer);
            assertEquals(1, (Integer)optional.get());
        }

        @Test
        @DisplayName("should return true when proxy object references are the same")
        public void proxyEqualsTest1() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            Class<ProxyInterface> proxyInterface = ProxyInterface.class;

            Method objectEqualsMethod = ProxyMethodUtils.getMethod(
                Object.class,
                "equals",
                Object.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );
            
            ProxyInterface proxy = externalizedProperties.proxy(proxyInterface);

            // proxy == proxy
            Object isEqual = handler.invoke(
                proxy, 
                objectEqualsMethod, 
                new Object[] { proxy }
            );

            assertTrue(isEqual instanceof Boolean);
            assertTrue((boolean)isEqual);
        }

        @Test
        @DisplayName("should return false when proxy object references are not same")
        public void proxyEqualsTest2() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            ProxyInterface proxy1 = 
                externalizedProperties.proxy(ProxyInterface.class);
            OtherProxyInterface proxy2 = 
                externalizedProperties.proxy(OtherProxyInterface.class);

            Method objectEqualsMethod = ProxyMethodUtils.getMethod(
                Object.class,
                "equals",
                Object.class
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            // proxy1 != proxy2
            Object isEqual = handler.invoke(
                proxy1, 
                objectEqualsMethod, 
                new Object[] { proxy2 }
            );

            assertTrue(isEqual instanceof Boolean);
            assertFalse((boolean)isEqual);
        }

        @Test
        @DisplayName(
            "should treat equals method with different signature as a proxy method"
        )
        public void proxyEqualsTest3() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            ProxyInterfaceWithEqualsMethodOverload proxy = 
                externalizedProperties.proxy(
                    ProxyInterfaceWithEqualsMethodOverload.class
                );

            Method objectEqualsMethodOverload = ProxyMethodUtils.getMethod(
                ProxyInterfaceWithEqualsMethodOverload.class,
                (ProxyMethodReference<ProxyInterfaceWithEqualsMethodOverload, Boolean>)
                ProxyInterfaceWithEqualsMethodOverload::equals
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            // equals method treated as proxy method
            // instead of an Object method due to different signature.
            assertThrows(
                UnresolvedPropertiesException.class,
                () -> handler.invoke(
                    proxy, 
                    objectEqualsMethodOverload, 
                    new Object[] { proxy }
                )
            );
        }

        @Test
        @DisplayName("should return proxy's identity hash code")
        public void proxyHashCodeTest1() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            ProxyInterface proxy = 
                externalizedProperties.proxy(ProxyInterface.class);

            Method objectHashCodeMethod = ProxyMethodUtils.getMethod(
                Object.class,
                "hashCode"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object hashCode = handler.invoke(
                proxy, 
                objectHashCodeMethod, 
                null
            );

            assertEquals(
                System.identityHashCode(proxy),
                hashCode
            );
        }

        @Test
        @DisplayName("should return standard Object.toString() format")
        public void proxyToStringTest1() throws Throwable {
            ResolverProvider<?> resolverProvider = MapResolver.provider(Collections.emptyMap());
            ConverterProvider<?> converterProvider = NoOpConverter.provider();
            ExternalizedProperties externalizedProperties = externalizedProperties(
                resolverProvider,
                converterProvider
            );

            ProxyInterface proxy = 
                externalizedProperties.proxy(ProxyInterface.class);

            Method objectToStringMethod = ProxyMethodUtils.getMethod(
                Object.class,
                "toString"
            );

            ExternalizedPropertiesInvocationHandler handler = 
                new ExternalizedPropertiesInvocationHandler(
                    rootResolver(
                        externalizedProperties,
                        resolverProvider
                    ),
                    rootConverter(
                        externalizedProperties,
                        converterProvider
                    )
                );

            Object toStringResult = handler.invoke(
                proxy, 
                objectToStringMethod, 
                null
            );

            assertEquals(
                objectToStringMethod.invoke(proxy),
                toStringResult
            );
        }
    }

    private Resolver rootResolver(
            ExternalizedProperties externalizedProperties,
            ResolverProvider<?>... resolverProviders
    ) {
        return new RootResolver(
            externalizedProperties,
            Arrays.asList(resolverProviders), 
            ep -> new RootProcessor(ep), 
            ep -> new SimpleVariableExpander(ep)
        );
    }

    private Converter<?> rootConverter(
            ExternalizedProperties externalizedProperties,
            ConverterProvider<?>... converterProviders
    ) {
        return new RootConverter(
            externalizedProperties,
            converterProviders
        );
    }

    private ExternalizedProperties externalizedProperties(
            ResolverProvider<?> resolverProvider,
            ConverterProvider<?> converterProvider
    ) { 
        return ExternalizedProperties.builder()
            .resolvers(resolverProvider)
            .converters(converterProvider)
            .build();
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty("property.with.default.value")
        default String propertyWithDefaultValueParameter(String defaultValue) {
            return defaultValue;
        }

        @ExternalizedProperty("property.with.default.value")
        default String propertyWithDefaultValue() {
            return "default.value";
        }

        // No annotation with constant default value.
        default String propertyWithNoAnnotationButWithDefaultValue() {
            return "default.value";
        }

        // No annotation but with default value parameter.
        default String propertyWithNoAnnotationButWithDefaultValueParameter(String defaultValue) {
            return defaultValue;
        }
    
        // No annotation and no default value.
        String propertyWithNoAnnotationAndNoDefaultValue();

        @ExternalizedProperty("property.int")
        int intProperty();

        @ExternalizedProperty("property-${custom.variable}")
        String variableProperty();

        @ExternalizedProperty("property.optional")
        Optional<String> optionalProperty();
        
        @ExternalizedProperty("property.optional.with.default.value")
        default Optional<String> optionalPropertyWithDefaultValue() {
            return Optional.of("default.value");
        }

        @ExternalizedProperty("property.optional.with.default.value")
        default Optional<String> optionalPropertyWithDefaultValueParameter(String defaultValue) {
            return Optional.ofNullable(defaultValue);
        }

        // No annotation with default value.
        default Optional<String> optionalPropertyWithNoAnnotationAndWithDefaultValue() {
            return Optional.of("default.value");
        }
    
        // No annotation with provided default value.
        default Optional<String> optionalPropertyWithNoAnnotationAndWithDefaultValueParameter(String defaultValue) {
            return Optional.ofNullable(defaultValue);
        }
    
        // No annotation ano no default value.
        Optional<String> optionalPropertyWithNoAnnotationAndNoDefaultValue();

        @ExternalizedProperty("property.optional.nonstring")
        Optional<Integer> nonStringOptionalProperty();
    }

    static interface OtherProxyInterface {}

    static interface ProxyInterfaceWithEqualsMethodOverload {
        boolean equals();
    }
}