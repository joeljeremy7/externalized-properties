package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Method handle factory to creating {@link MethodHandle} for methods.
 * This factory changes implementation based on the detected java version.
 * 
 * This should continue working even when running under Java 9+.
 */
public class MethodHandleFactory {
    // Not Java 1.7, 1.8, etc.
    private static final boolean IS_RUNNING_ON_JAVA_9_OR_LATER = 
        !System.getProperty("java.specification.version").startsWith("1.");
    // This is null if not on Java 9+.
    // This method should be present in Java 9+.
    private static final Method JAVA_9_MH_PRIVATE_LOOKUP_METHOD;

    static {
        Method m = null;
        try {
            if (IS_RUNNING_ON_JAVA_9_OR_LATER) {
                m = MethodHandles.class.getDeclaredMethod(
                    "privateLookupIn", 
                    Class.class, 
                    Lookup.class
                );
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Unable to find MethodHandles.privateLookupIn while running on Java 9+.", 
                e
            );
        } finally {
            JAVA_9_MH_PRIVATE_LOOKUP_METHOD = m;
        }
    }

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
            methodHandle = buildMethodHandleInternal(method);
            weakMethodHandleCache.putIfAbsent(method, methodHandle);
        }
        return methodHandle;
    }

    private static MethodHandle buildMethodHandleInternal(Method method) {
        if (IS_RUNNING_ON_JAVA_9_OR_LATER) {
            Lookup privateLookup = getPrivateLookup(method.getDeclaringClass());
            try {
                return privateLookup.in(method.getDeclaringClass())
                    .unreflectSpecial(method, method.getDeclaringClass());
            } catch (Throwable ex) {
                throw new ExternalizedPropertiesException(
                    String.format(
                        "Error occurred while trying to build method handle. " +
                        "Externalized property method: %s.",
                        method.toGenericString()
                    ), 
                    ex
                );
            }
        }

        try {
            // This will only work on Java 8.
            // For Java9+, the new private lookup API should be used.
            final Constructor<Lookup> constructor = Lookup.class
                .getDeclaredConstructor(Class.class);
            
            constructor.setAccessible(true);
            final Lookup lookup = constructor.newInstance(method.getDeclaringClass());
            constructor.setAccessible(false);
            
            return lookup.in(method.getDeclaringClass())
                .unreflectSpecial(method, method.getDeclaringClass());
        } catch (Throwable ex) {
            throw new ExternalizedPropertiesException(
                "Error occurred while trying to build method handle.", 
                ex
            );
        }
    }

    private static Lookup getPrivateLookup(Class<?> classToLookup) {
        Lookup privateLookup = null;
        try {
            if (JAVA_9_MH_PRIVATE_LOOKUP_METHOD != null) {
                // Null obj since MethodHandles.privateLookupIn is static.
                privateLookup = (Lookup)JAVA_9_MH_PRIVATE_LOOKUP_METHOD.invoke(null, new Object[] { 
                    classToLookup,
                    MethodHandles.lookup()
                });
            }

        } catch (Exception ex) {
            throw new IllegalStateException(
                "Failed to obtain Java 9+ MethodHandles.privateLookupIn method.", 
                ex
            );
        }
            
        if (privateLookup == null) {
            throw new IllegalStateException(
                "Failed to obtain Java 9+ MethodHandles.privateLookupIn method."
            );
        }

        return privateLookup;
    }
}