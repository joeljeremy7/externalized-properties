package io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.WeakConcurrentMapCacheStrategy.WeakKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Nested
public class WeakConcurrentMapCacheStrategyTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should not throw when invoking default constructor")
        public void test1() {
            assertDoesNotThrow(
                () -> new WeakConcurrentMapCacheStrategy<>()
            );
        }

        @Test
        @DisplayName("should throw when cache argument argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new WeakConcurrentMapCacheStrategy<>(null)
            );
        }
    }

    @Nested
    class CacheMethod {
        @Test
        @DisplayName("should cache value to the cache map")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentMapCacheStrategy<>();
    
            cacheStrategy.cache(cacheKey, cacheValue);
    
            assertEquals(
                cacheValue, 
                cacheStrategy.get(cacheKey).get()
            );
        }

        @Test
        @DisplayName("should automatically remove cache key when weak references are cleared")
        public void test2() {
            // Use String constructor to explicitly create
            // new String instance and prevent string interning.
            // This allows GC to clear this reference when set to null.
            String cacheKey1 = new String("cache.key.1");
            String cacheKey2 = new String("cache.key.2");
    
            ConcurrentMap<WeakKey<String>, String> cache = 
                new ConcurrentHashMap<>();
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentMapCacheStrategy<>(cache);
    
            cacheStrategy.cache(cacheKey1, "cache.value.1");
            cacheStrategy.cache(cacheKey2, "cache.value.2");

            assertTrue(cacheStrategy.get(cacheKey1).isPresent());
            assertTrue(cacheStrategy.get(cacheKey2).isPresent());

            // Clear references.
            cacheKey1 = null;
            cacheKey2 = null;

            assertTimeoutPreemptively(Duration.ofSeconds(86400), () -> {
                // Wait for GC to clear references.
                // Call get here so cache strategy does its internal purging of keys.
                // Use string literals here because original cache key instances were nulled.
                while (cacheStrategy.get("cache.key.1").isPresent() ||
                    cacheStrategy.get("cache.key.2").isPresent()
                ) {
                    System.gc();
                }
            });

            // Use string literals here because original cache key instances were nulled.
            assertFalse(cacheStrategy.get("cache.key.1").isPresent());
            assertFalse(cacheStrategy.get("cache.key.2").isPresent());
            assertTrue(cache.isEmpty());
        }
    }

    @Nested
    class GetMethod {
        @Test
        @DisplayName("should return cached value from the cache map")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentMapCacheStrategy<>();

            // Cache.
            cacheStrategy.cache(cacheKey, cacheValue);
    
            // Retrieve.
            Optional<String> cachedPropertyValue = 
                cacheStrategy.get(cacheKey);
    
            assertTrue(cachedPropertyValue.isPresent());
            assertSame(
                cacheValue, 
                cachedPropertyValue.get()
            );
        }

        @Test
        @DisplayName("should return empty Optional when key is not found in cache map")
        public void test2() {
            String cacheKey = "cache.key";
    
            // Empty cache.
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentMapCacheStrategy<>();
    
            Optional<String> cachedPropertyValue = 
                cacheStrategy.get(cacheKey);
    
            assertFalse(cachedPropertyValue.isPresent());
        }
    }

    @Nested
    class ExpireMethod {
        @Test
        @DisplayName("should expire cached value from the cache map")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
            cache.put(cacheKey, cacheValue);
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentMapCacheStrategy<>();

            // Cache.
            cacheStrategy.cache(cacheKey, cacheValue);
    
            // Expire the cached key.
            cacheStrategy.expire(cacheKey);
    
            // Deleted from cache map.
            assertFalse(cacheStrategy.get(cacheKey).isPresent());
        }
    }

    @Nested
    class ExpireAllMethod {
        @Test
        @DisplayName("should expire all cached values from the cache map")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "property.value";
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentMapCacheStrategy<>();
            
            // Cache.
            cacheStrategy.cache(cacheKey, cacheValue);
    
            // Expire all cached items.
            cacheStrategy.expireAll();
    
            // All items deleted from cache map.
            assertFalse(cacheStrategy.get(cacheKey).isPresent());
        }
    }

    @Nested
    class WeakKeyClass {
        @Nested
        class EqualsMethod {
            @Test
            @DisplayName("should return true when WeakKey referents are equal")
            public void test1() {
                String referent = "referent";
                WeakConcurrentMapCacheStrategy.WeakKey<String> key = 
                    new WeakConcurrentMapCacheStrategy.WeakKey<>(referent);

                WeakConcurrentMapCacheStrategy.WeakKey<String> sameReferentKey = 
                    new WeakConcurrentMapCacheStrategy.WeakKey<>(referent);

                assertTrue(key.equals(sameReferentKey));
            }

            @Test
            @DisplayName("should return false when WeakKey referents are not equal")
            public void test2() {
                String referent1 = "referent1";
                WeakConcurrentMapCacheStrategy.WeakKey<String> key = 
                    new WeakConcurrentMapCacheStrategy.WeakKey<>(referent1);

                String referent2 = "referent2";
                WeakConcurrentMapCacheStrategy.WeakKey<String> otherKey = 
                    new WeakConcurrentMapCacheStrategy.WeakKey<>(referent2);

                assertFalse(key.equals(otherKey));
            }

            @Test
            @DisplayName("should return false when object is not a WeakKey")
            public void test3() {
                String referent = "referent";
                WeakConcurrentMapCacheStrategy.WeakKey<String> key = 
                    new WeakConcurrentMapCacheStrategy.WeakKey<>(referent);

                assertFalse(key.equals(new Object()));
            }
        }
    }

    public static class CacheKey {}
}