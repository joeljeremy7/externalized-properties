package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedPropertiesException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyCollection;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyString;

/**
 * The resolver result object containing the resolved and unresolved properties.
 */
public class ResolverResult {
    /**
     * Empty result with no resolved or unresolved properties.
     */
    public static final ResolverResult EMPTY = new ResolverResult();

    private final Map<String, String> resolvedPropertiesByName;
    private final Set<String> unresolvedPropertyNames;
    
    /**
     * Constructor.
     * 
     * @param requestedPropertyNames The requested property names.
     * @param resolvedPropertiesByName The map of resolved properties keyed by 
     * property names.
     */
    private ResolverResult(
            Collection<String> requestedPropertyNames,
            Map<String, String> resolvedPropertiesByName
    ) {
        requireNonNull(requestedPropertyNames, "requestedPropertyNames");
        requireNonNull(resolvedPropertiesByName, "resolvedPropertiesByName");

        this.resolvedPropertiesByName = resolvedPropertiesByName;
        this.unresolvedPropertyNames = getUnresolvedPropertyNames(
            requestedPropertyNames, 
            resolvedPropertiesByName.keySet()
        );
    }

    /** For {@link ResolverResult#EMPTY} */
    private ResolverResult() {
        this.resolvedPropertiesByName = Collections.emptyMap();
        this.unresolvedPropertyNames = Collections.emptySet();
    }

    /**
     * Returns the resolved properties map. This is an unmodifiable map
     * so any attempt to change this map will result in an exception.
     * 
     * @return The unmodifiable map of resolved properties.
     */
    public Map<String, String> resolvedProperties() {
        return Collections.unmodifiableMap(resolvedPropertiesByName);
    }

    /**
     * Returns the set of resolved property names. This is an unmodifiable set
     * so any attempt to change this set will result in an exception.
     * 
     * @return The unmodifiable set of resolved property names.
     */
    public Set<String> resolvedPropertyNames() {
        return Collections.unmodifiableSet(resolvedPropertiesByName.keySet());
    }

    /**
     * Returns the set of unresolved property names. This is an unmodifiable set
     * so any attempt to change this set will result in an exception.
     * 
     * @return The unmodifiable set of unresolved property names.
     */
    public Set<String> unresolvedPropertyNames() {
        return Collections.unmodifiableSet(unresolvedPropertyNames);
    }

    /**
     * Check if there are any resolved properties.
     * 
     * @return {@code true}, if there are any resolved properties. Otherwise, {@code false}.
     */
    public boolean hasResolvedProperties() {
        return !resolvedPropertiesByName.isEmpty();
    }

    /**
     * Check if there are any unresolved properties.
     * 
     * @return {@code true}, if there are any unresolved properties. Otherwise, {@code false}.
     */
    public boolean hasUnresolvedProperties() {
        return !unresolvedPropertyNames.isEmpty();
    }

    /**
     * Find resolved property with the given name.
     * 
     * @param propertyName The name of the property.
     * @return An optional containing the resolved property value if it is in the result. 
     * Otherwise, an empty optional instance.
     */
    public Optional<String> findResolvedProperty(String propertyName) {
        return Optional.ofNullable(resolvedPropertiesByName.get(propertyName));
    }

    /**
     * Find resolved property with the given name or else throw an exception 
     * if the property has not been resolved.
     * 
     * @param propertyName The name of the property.
     * @return The resolved property value if it is in the result.
     * Otherwise an {@link UnresolvedPropertiesException} will be thrown.
     * @throws UnresolvedPropertiesException if the requested property is not in the result.
     */
    public String findRequiredProperty(String propertyName) {
        String resolvedPropertyValue = resolvedPropertiesByName.get(propertyName);
        if (resolvedPropertyValue == null) {
            throw new UnresolvedPropertiesException(
                propertyName,
                "Requested property named " + propertyName + " has not been resolved."
            );
        }
        return resolvedPropertyValue;
    }

    /**
     * Builder.
     * 
     * @param requestedPropertyNames The names of the properties that were requested 
     * to be resolved.
     * @return The builder.
     */
    public static Builder builder(String... requestedPropertyNames) {
        return new Builder(requestedPropertyNames == null ?
            Collections.emptyList() :
            Arrays.asList(requestedPropertyNames)
        );
    }

    /**
     * Builder.
     * 
     * @param requestedPropertyNames The names of the properties that were requested 
     * to be resolved.
     * @return The builder.
     */
    public static Builder builder(Collection<String> requestedPropertyNames) {
        return new Builder(requestedPropertyNames);
    }

    private static Set<String> getUnresolvedPropertyNames(
            Collection<String> propertiesToResolve,
            Set<String> resolvedPropertyNames
    ) {
        Set<String> unresolvedPropertyNames = new HashSet<>(
            // Prevent internal hashmap resizing.
            (int) ((float) propertiesToResolve.size() / 0.75F + 1.0F)
        );

        for (String propertyName : propertiesToResolve) {
            if (!resolvedPropertyNames.contains(propertyName)) {
                unresolvedPropertyNames.add(propertyName);
            }
        }

        return unresolvedPropertyNames;
    }

    /**
     * The {@link ResolverResult} builder.
     */
    public static class Builder {
        private final Collection<String> requestedPropertyNames;
        private final Map<String, String> resolvedPropertiesByName;

        /**
         * Constructor.
         * 
         * @param requestedPropertyNames The requested property names.
         */
        private Builder(Collection<String> requestedPropertyNames) {
            this.requestedPropertyNames = requireNonNullOrEmptyCollection(
                requestedPropertyNames, 
                "requestedPropertyNames"
            );

            this.resolvedPropertiesByName = new HashMap<>(
                // Prevent hashmap resizing.
                // 0.75 is HashMap's default load factor.
                (int)(requestedPropertyNames.size() / 0.75f) + 1
            );
        }

        /**
         * Add resolved property to the result.
         * 
         * @param propertyName The property name.
         * @param resolvedPropertyValue The resolved property value.
         * @return This builder.
         */
        public Builder add(String propertyName, String resolvedPropertyValue) {
            requireNonNullOrEmptyString(propertyName, "propertyName");
            requireNonNull(resolvedPropertyValue, "resolvedPropertyValue");

            this.resolvedPropertiesByName.put(propertyName, resolvedPropertyValue);
            return this;
        }

        /**
         * Add all resolved properties to the result.
         * 
         * @param resolvedPropertiesByName The map containing the resolved property values
         * keyed by the property names.
         * @return This builder.
         */
        public Builder addAll(Map<String, String> resolvedPropertiesByName) {
            requireNonNull(resolvedPropertiesByName, "resolvedPropertiesByName");

            this.resolvedPropertiesByName.putAll(resolvedPropertiesByName);
            return this;
        }

        /**
         * Build the {@link ResolverResult}.
         * 
         * @return The built {@link ResolverResult}.
         */
        public ResolverResult build() {
            return new ResolverResult(
                requestedPropertyNames,
                resolvedPropertiesByName
            );
        }
    }
}