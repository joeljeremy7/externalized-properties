package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.MethodUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MethodHandleFactoryTests {
    private final Method TEST_METHOD = MethodUtils.getMethod(
        MethodHandleFactoryTests.class,
        "testMethod",
        String.class
    );

    private final Method TEST_DEFAULT_INTERFACE_METHOD = MethodUtils.getMethod(
        DefaultMethodInterface.class,
        DefaultMethodInterface::test
    );

    private final Method TEST_NON_DEFAULT_INTERFACE_METHOD = MethodUtils.getMethod(
        NonDefaultMethodInterface.class,
        NonDefaultMethodInterface::test
    );

    @Nested
    class CreateMethodHandleMethod {
        @Test
        @DisplayName("should create a method handle which invokes the target method")
        void test1() throws Throwable {
            MethodHandleFactory methodHandleFactory = new MethodHandleFactory();
            MethodHandle methodHandle = 
                methodHandleFactory.createMethodHandle(TEST_METHOD);

            String value = "test";
            // Invoke the method handle.
            String result = (String)methodHandle.invokeExact(
                MethodHandleFactoryTests.this,
                value
            );

            assertEquals(value, result);
        }

        @Test
        @DisplayName(
            "should create a method handle which invokes the target " + 
            "default interface method"
        )
        void test2() throws Throwable {
            MethodHandleFactory methodHandleFactory = new MethodHandleFactory();
            MethodHandle methodHandle = 
                methodHandleFactory.createMethodHandle(TEST_DEFAULT_INTERFACE_METHOD);

            // Anonymous class.
            DefaultMethodInterface dummy = new DefaultMethodInterface() {};

            String value = "test";
            // Invoke the method handle.
            String result = (String)methodHandle.invokeExact(dummy, value);

            assertEquals(value, result);
        }

        @Test
        @DisplayName("should cache method handles per method")
        void test3() throws Throwable {
            MethodHandleFactory methodHandleFactory = new MethodHandleFactory();
            MethodHandle methodHandle1 = 
                methodHandleFactory.createMethodHandle(TEST_METHOD);
            MethodHandle methodHandle2 = 
                methodHandleFactory.createMethodHandle(TEST_METHOD);

            assertSame(methodHandle1, methodHandle2);
        }

        @Test
        @DisplayName("should throw when method is a non-default/abstract method")
        void test4() throws Throwable {
            MethodHandleFactory methodHandleFactory = new MethodHandleFactory();
            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> methodHandleFactory.createMethodHandle(TEST_NON_DEFAULT_INTERFACE_METHOD)
            );
        }
    }

    /**
     * Created method handles will invoke this method. We will track the names of the 
     * tests which invoked this method so we can assert.
     * 
     * @param value The name of the test which invoked the method.
     */
    String testMethod(String value) {
        return value;
    }

    private static interface DefaultMethodInterface {
        default String test(String defaultValue) {
            return defaultValue;
        }
    }

    private static interface NonDefaultMethodInterface {
        String test();
    }
}
