package io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.DaemonThreadFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * A {@link CacheStrategy} decorator that automatically expires cached
 * items after the given cache item lifetime duration.
 */
public class ExpiringCacheStrategy<K, V> implements CacheStrategy<K, V> {

    private final ScheduledExecutorService expiryScheduler = 
        Executors.newSingleThreadScheduledExecutor(
            new DaemonThreadFactory(ExpiringCacheStrategy.class.getName())
        );
    private final CacheStrategy<K, V> decorated;
    private final Duration cacheItemLifetime;

    /**
     * Constructor.
     * 
     * @param decorated The decorated {@link CacheStrategy}.
     * @param cacheItemLifetime The allowed duration of cache items in the cache.
     */
    public ExpiringCacheStrategy(
            CacheStrategy<K, V> decorated,
            Duration cacheItemLifetime
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cacheItemLifetime = requireNonNull(cacheItemLifetime, "cacheItemLifetime");
    }

    /**
     * Cache the value associated to the key and schedule individual keys for expiry 
     * based on the configured cache item lifetime.
     * 
     * @param cacheKey The cache key associated to the value.
     * @param value The value to cache.
     */
    @Override
    public void cache(K cacheKey, V value) {
        decorated.cache(cacheKey, value);
        scheduleForExpiry(cacheKey);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<V> get(K cacheKey) {
        return decorated.get(cacheKey);
    }

    /** {@inheritDoc} */
    @Override
    public void expire(K cacheKey) {
        decorated.expire(cacheKey);
    }

    /** {@inheritDoc} */
    @Override
    public void expireAll() {
        decorated.expireAll();
    }

    private void scheduleForExpiry(K cacheKey) {
        expiryScheduler.schedule(
            () -> expire(cacheKey), 
            cacheItemLifetime.toMillis(), 
            TimeUnit.MILLISECONDS
        );
    }
}
