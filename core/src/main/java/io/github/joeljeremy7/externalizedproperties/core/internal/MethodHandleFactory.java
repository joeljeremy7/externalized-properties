package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Method handle factory to creating {@link MethodHandle} for methods.
 */
public class MethodHandleFactory {
    private final Map<Method, MethodHandle> weakMethodHandleCache = new WeakHashMap<>(); 

    /**
     * Build a method handle from the given target and method.
     * 
     * @param method The method to build the method handle from.
     * @return The generated {@link MethodHandle} for the method. 
     * This method handle has been binded to the target object. 
     */
    public MethodHandle createMethodHandle(Method method) {
        MethodHandle methodHandle = weakMethodHandleCache.get(method);
        if (methodHandle == null) {
            methodHandle = buildMethodHandle(method);
            weakMethodHandleCache.putIfAbsent(method, methodHandle);
        }
        return methodHandle;
    }

    private static MethodHandle buildMethodHandle(Method method) {
        try {
            Lookup privateLookup = MethodHandles.privateLookupIn(
                method.getDeclaringClass(), 
                MethodHandles.lookup()
            );
            return privateLookup.unreflectSpecial(method, method.getDeclaringClass());
        } catch (Throwable ex) {
            throw new ExternalizedPropertiesException(
                "Error occurred while trying to build method handle for method: " +
                method.toGenericString(), 
                ex
            );
        }
    }
}