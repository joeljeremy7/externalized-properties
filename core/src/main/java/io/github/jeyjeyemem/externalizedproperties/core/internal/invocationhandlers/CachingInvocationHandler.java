package io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Implementation of {@link InvocationHandler} that caches invocation results.
 */
public class CachingInvocationHandler implements InvocationHandler {

    private final InvocationHandler decorated;
    private final CacheStrategy<Method, Object> cacheStrategy;

    /**
     * Constructor.
     * 
     * @param decorated The decorated {@link InvocationHandler} instance.
     * @param cacheStrategy The cache strategy keyed by a {@link Method} and whose values
     * are the resolved properties. This cache strategy should weakly hold on to the 
     * {@link Method} key in order to avoid leaks and class unloading issues. 
     */
    public CachingInvocationHandler(
            InvocationHandler decorated,
            CacheStrategy<Method, Object> cacheStrategy
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
    }

    /** {@inheritDoc} */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Optional<Object> cached = cacheStrategy.get(method);
        if (cached.isPresent()) {
            return cached.get();
        }

        Object result = decorated.invoke(proxy, method, args);
        if (result != null) {
            cacheStrategy.cache(method, result);
        }

        return result;
    }
}
