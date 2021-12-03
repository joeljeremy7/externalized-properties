package io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.internal.ExternalizedPropertyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.internal.MethodHandleFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Invocation handler for Externalized Properties.
 * This handles invocations of methods that are marked with {@link ExternalizedProperty} annotation.
 */
public class ExternalizedPropertyInvocationHandler implements InvocationHandler {
    private final ExternalizedProperties externalizedProperties;
    private final MethodHandleFactory methodHandleFactory = new MethodHandleFactory();

    /**
     * Constructor.
     * 
     * @param externalizedProperties The externalized properties.
     */
    public ExternalizedPropertyInvocationHandler(
            ExternalizedProperties externalizedProperties
    ) {
        this.externalizedProperties = requireNonNull(
            externalizedProperties,
            "externalizedProperties"
        );
    }

    /**
     * Handles the externalized property method invocation.
     * 
     * @param proxy The proxy object.
     * @param method The invoked method.
     * @param args The method invocation arguments.
     * @return Method return value.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle invocations to native Object methods:
        // toString, equals, hashCode
        Object objectMethodResult = handleIfObjectMethod(proxy, method, args);
        if (objectMethodResult != null) {
            return objectMethodResult;
        }

        ExternalizedPropertyMethod propertyMethod = ExternalizedPropertyMethod.create(
            proxy, 
            method,
            externalizedProperties,
            methodHandleFactory
        );

        return propertyMethod.resolveProperty(args);
    }
    
    // Avoid calling methods in proxy object to avoid recursion.
    private static Object handleIfObjectMethod(Object proxy, Method method, Object[] args) {
        if ("toString".equals(method.getName())) {
            return proxyToString(proxy, args);
        }
        else if ("equals".equals(method.getName()) && 
                hasMethodParameters(method, Object.class)) {
            return proxyEquals(proxy, args);
        }
        else if ("hashCode".equals(method.getName())) {
            return proxyHashCode(proxy, args);
        }

        return null;
    }

    private static int proxyHashCode(Object proxy, Object[] args) {
        return System.identityHashCode(proxy);
    }

    // Only do reference equality.
    private static boolean proxyEquals(Object proxy, Object[] args) {
        return proxy == args[0];
    }

    private static String proxyToString(Object proxy, Object[] args) {
        return proxy.getClass().getName() + '@' + 
            Integer.toHexString(proxyHashCode(proxy, args));
    }

    private static boolean hasMethodParameters(Method method, Class<?>... parameterTypes) {
        return Arrays.equals(method.getParameterTypes(), parameterTypes);
    }
}