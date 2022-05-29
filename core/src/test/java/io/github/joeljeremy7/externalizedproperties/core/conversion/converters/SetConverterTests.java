package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.SetConverter.SetFactory;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.TestProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SetConverterTests {
    private static final TestProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new TestProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when set factory argument is null.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new SetConverter(null)
            );
        }
    }

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        void test1() {
            SetConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Set class.")
        void test2() {
            SetConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Set.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not a Set class.")
        void test4() {
            SetConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should convert value to a Set.")
        void test1() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3")), 
                set
            );
        }

        @Test
        @DisplayName(
            "should convert to Set<String> when target type has no " + 
            "type parameters i.e. Set.class"
        )
        void test2() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setInteger,
                externalizedProperties(converter)
            );
                
            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "1,2,3",
                // Override proxy method return type with a raw Set
                // No generic type parameter
                Set.class
            );

            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            // Strings and not Integers.
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("1", "2", "3"), 
                set
            );
        }

        @Test
        @DisplayName("should convert value to a Set using custom delimiter.")
        void test3() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setCustomDelimiter,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "value1#value2#value3"
            );

            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3")), 
                set
            );

        }

        @Test
        @DisplayName("should convert value according to the Set's generic type parameter.")
        void test4() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setInteger,
                externalizedProperties(
                    converter,
                    new PrimitiveConverter()
                )
            );
            
            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "1,2,3"
            );
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof Integer));
            assertIterableEquals(
                Arrays.asList(1, 2, 3), 
                set
            );

        }

        @Test
        @DisplayName(
            "should return String values when Set's generic type parameter is a wildcard."
        )
        void test5() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setPropertyWildcard,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3")), 
                set
            );

        }

        @Test
        @DisplayName("should return String values when Set's generic type parameter is Object.")
        void test6() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setPropertyObject,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
        
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3")), 
                set
            );

        }

        @Test
        @DisplayName("should return empty Set when property value is empty.")
        void test7() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "" // Empty value.
            );
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertTrue(set.isEmpty());
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        void test8() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3,,value5" // Has empty values.
            );
        
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(5, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new LinkedHashSet<>(
                    Arrays.asList("value1", "value2", "value3", "", "value5")
                ), 
                set
            );

        }

        @Test
        @DisplayName("should strip empty values when annotated with @StripEmptyValues.")
        void test9() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setPropertyStripEmpty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "value1,,value3,,value5" // Has empty values.
            );
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new LinkedHashSet<>(Arrays.asList("value1", "value3", "value5")), 
                set
            );
        }

        @Test
        @DisplayName(
            "should throw when no rootConverter is registered that can handle " + 
            "the Set's generic type parameter."
        )
        void test10() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setInteger,
                externalizedProperties(converter)
            );
            
            // No registered rootConverter for Integer.
            assertThrows(
                ExternalizedPropertiesException.class,
                () -> converter.convert(proxyMethod, "1,2,3,4,5")
            );
        }

        @Test
        @DisplayName(
            "should convert value according to the Set's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. Set<Optional<String>>."
        )
        void test11() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setPropertyNestedGenerics, // Returns a Set<Optional<String>>.
                externalizedProperties(converter)
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof Optional<?>));
            assertIterableEquals(
                new LinkedHashSet<>(Arrays.asList(
                    Optional.of("value1"), 
                    Optional.of("value2"), 
                    Optional.of("value3")
                )), 
                set
            );
        }

        @Test
        @DisplayName(
            "should convert value according to the Set's generic type parameter. " + 
            "Generic type parameter is generic array e.g. Set<Optional<String>[]>."
        )
        void test12() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setPropertyNestedGenericsArray, // Returns a Set<Optional<String>[]>.
                externalizedProperties(
                    converter,
                    new ArrayConverter()
                )
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof Optional<?>[]));

            // Set is a set of arrays (Set<Optional<String>[]>).
            // Convert to List for set content assertion.
            List<?> setAsList = new ArrayList<>(set);
            Optional<?>[] item1 = (Optional<?>[])setAsList.get(0);
            Optional<?>[] item2 = (Optional<?>[])setAsList.get(1);
            Optional<?>[] item3 = (Optional<?>[])setAsList.get(2);

            assertArrayEquals(
                new Optional<?>[] { Optional.of("value1") }, 
                item1
            );

            assertArrayEquals(
                new Optional<?>[] { Optional.of("value2") }, 
                item2
            );
            
            assertArrayEquals(
                new Optional<?>[] { Optional.of("value3") }, 
                item3
            );
        }

        @Test
        @DisplayName("should throw when target type has a type variable e.g. Set<T>.")
        void test13() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setPropertyT,
                externalizedProperties(converter)
            );
                
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(proxyMethod, "value")
            );
        }



        @Test
        @DisplayName("should discard duplicate values.")
        void test14() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "value1,value1,value1,value1,value5" // There are 4 value1
            );
        
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(2, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new LinkedHashSet<>(Arrays.asList("value1", "value5")), 
                set
            );

        }

        /**
         * Set factory tests.
         */
        
        @Test
        @DisplayName(
            "should use provided set factory to construct sets."
        )
        void setFactoryTest1() {
            SetConverter converter = converterToTest(
                // Uses CopyOnWriteArraySet.
                capacity -> new CopyOnWriteArraySet<>()
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends Set<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3,value4,value5"
            );
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            // Default: Should use ',' as delimiter and will not strip empty values.
            // This will strip trailing empty values though.
            assertNotNull(set);
            assertTrue(set instanceof CopyOnWriteArraySet);
            assertEquals(5, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new CopyOnWriteArraySet<>(Arrays.asList("value1", "value2", "value3", "value4", "value5")), 
                set
            );
        }
        
        @Test
        @DisplayName(
            "should throw when provided set factory returns null."
        )
        void setFactoryTest2() {
            SetConverter converter = converterToTest(
                // Returns null.
                capacity -> null
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setProperty,
                externalizedProperties(converter)
            );

            // Throws IllegalStateException if set factory returned null.
            assertThrows(
                IllegalStateException.class, 
                () -> converter.convert(proxyMethod, "value1,value2,value3")
            );
        }
        
        @Test
        @DisplayName(
            "should throw when provided set factory returns a populated set."
        )
        void setFactoryTest3() {
            SetConverter converter = converterToTest(
                // Returns a populated set.
                capacity -> new HashSet<>(Arrays.asList(
                    "should", "not", "be", "populated"
                ))
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setProperty,
                externalizedProperties(converter)
            );

            // Throws IllegalStateException if set factory returned a populated set.
            assertThrows(
                IllegalStateException.class, 
                () -> converter.convert(proxyMethod, "value1,value2,value3")
            );
        }
    }

    private static SetConverter converterToTest(SetFactory setFactory) { 
        return new SetConverter(setFactory);
    }

    private static SetConverter converterToTest() { 
        return new SetConverter();
    }

    private static ExternalizedProperties externalizedProperties(
            SetConverter converterToTest,
            Converter<?>... additionalConverters
    ) {
        return ExternalizedProperties.builder()
            .converters(converterToTest)
            .converters(additionalConverters)
            .build();
    }

    static interface ProxyInterface {
        @ExternalizedProperty("property.set")
        Set<String> setProperty();
    
        @ExternalizedProperty("property.set.object")
        Set<Object> setPropertyObject();
    
        @ExternalizedProperty("property.set.custom.delimiter")
        @Delimiter("#")
        Set<String> setCustomDelimiter();
    
        @ExternalizedProperty("property.set.integer")
        Set<Integer> setInteger();
    
        @ExternalizedProperty("property.set.wildcard")
        Set<?> setPropertyWildcard();
    
        @ExternalizedProperty("property.set.stripempty")
        @StripEmptyValues
        Set<String> setPropertyStripEmpty();
    
        @ExternalizedProperty("property.set.nested.generics")
        Set<Optional<String>> setPropertyNestedGenerics();
    
        @ExternalizedProperty("property.set.nested.generics.array")
        Set<Optional<String>[]> setPropertyNestedGenericsArray();
    
        @ExternalizedProperty("property.set.T")
        <T> Set<T> setPropertyT();
    }
}
