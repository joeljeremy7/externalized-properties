package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DoubleConverterTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return true when target type is a Double.")
        void test1() {
            DoubleConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Double.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive double.")
        void test2() {
            DoubleConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Double.TYPE);
            assertTrue(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should convert value to a Double.")
        void test1() {
            DoubleConverter converter = converterToTest();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::doubleWrapperProperty, // This method returns a Double wrapper class
                externalizedProperties(converter)
            );

            ConversionResult<?> result = converter.convert(
                context,
                "1"
            );
            
            assertNotNull(result);
            Object wrapperValue = result.value();
            assertNotNull(wrapperValue);
            assertTrue(wrapperValue instanceof Double);
            assertEquals((double)1, (Double)wrapperValue);
        }

        @Test
        @DisplayName("should convert value to a primitive double.")
        void test2() {
            DoubleConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::doublePrimitiveProperty, // This method returns an double primitive
                externalizedProperties(converter)
            );

            ConversionResult<?> result = converter.convert(
                context,
                "2"
            );
            
            assertNotNull(result);
            Object primitiveValue = result.value();
            assertNotNull(primitiveValue);
            assertTrue(primitiveValue instanceof Double);
            assertEquals((double)2, (double)primitiveValue);
        }

        @Test
        @DisplayName("should throw when value is not a valid Double/double.")
        void test3() {
            DoubleConverter converter = converterToTest();
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::doublePrimitiveProperty, // This method returns an double primitive
                externalizedProperties(converter)
            );

            assertThrows(
                NumberFormatException.class, 
                () -> converter.convert(
                    context,
                    "invalid_value"
                )
            );
        }
    }

    private static DoubleConverter converterToTest() {
        return new DoubleConverter();
    }

    private static ExternalizedProperties externalizedProperties(
            DoubleConverter converterToTest
    ) {
        return ExternalizedProperties.builder()
            .converters(converterToTest)
            .build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property.double.primitive")
        double doublePrimitiveProperty();
    
        @ExternalizedProperty("property.double.wrapper")
        Double doubleWrapperProperty();
    }
}
