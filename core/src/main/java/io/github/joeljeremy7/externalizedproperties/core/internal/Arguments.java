package io.github.joeljeremy7.externalizedproperties.core.internal;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

/**
 * Arguments-related utility methods.
 */
public class Arguments {
    private Arguments(){}

    /**
     * Require argument to not be {@code null}.
     * 
     * @param <T> The type of the argument.
     * @param arg The argument.
     * @param argName The name of the argument to be used in building the 
     * {@link IllegalArgumentException} message if the argument failed validation.
     * @return The argument.
     */
    public static <T> T requireNonNull(@Nullable T arg, String argName) {
        if (arg == null) {
            throw new IllegalArgumentException(argName + " must not be null.");
        }
        return arg;
    }

    /**
     * Require argument to not be {@code null} or an empty {@link String}.
     * 
     * @param arg The {@link String} argument.
     * @param argName The name of the {@link String} argument to be used in building the 
     * {@link IllegalArgumentException} message if the argument failed validation.
     * @return The {@link String} argument.
     */
    public static String requireNonNullOrEmpty(@Nullable String arg, String argName) {
        if (arg == null || arg.isEmpty()) {
            throw new IllegalArgumentException(argName + " must not be null or empty.");
        }
        return arg;
    }

    /**
     * Require argument to not be {@code null} or an empty {@link String}.
     * 
     * @param arg The {@link String} argument.
     * @param argName The name of the {@link String} argument to be used in building the 
     * {@link IllegalArgumentException} message if the argument failed validation.
     * @return The {@link String} argument.
     */
    public static String requireNonNullOrBlank(@Nullable String arg, String argName) {
        if (arg == null || arg.isBlank()) {
            throw new IllegalArgumentException(argName + " must not be null or empty.");
        }
        return arg;
    }

    /**
     * Require argument to not be {@code null} or an empty {@link Collection}.
     * 
     * @param <T> The type of the collection argument.
     * @param arg The {@link Collection} argument.
     * @param argName The name of the {@link Collection} argument to be used in building the 
     * {@link IllegalArgumentException} message if the argument failed validation.
     * @return The {@link Collection} argument.
     */
    public static <T> Collection<T> requireNonNullOrEmpty(
            @Nullable Collection<T> arg, 
            String argName
    ) {
        if (arg == null || arg.isEmpty()) {
            throw new IllegalArgumentException(argName + " must not be null or empty.");
        }
        return arg;
    }

    /**
     * Require argument to not be {@code null} or an empty array.
     * 
     * @param <T> The type of the array argument.
     * @param arg The array argument.
     * @param argName The name of the array argument to be used in building the 
     * {@link IllegalArgumentException} message if the argument failed validation.
     * @return The array argument.
     */
    public static <T> T[] requireNonNullOrEmpty(
            @Nullable T[] arg, 
            String argName
    ) {
        if (arg == null || arg.length == 0) {
            throw new IllegalArgumentException(argName + " must not be null or empty.");
        }
        return arg;
    }

    /**
     * Require array argument to have no {@code null} elements.
     * 
     * @param <T> The type of the array argument.
     * @param arg The array argument.
     * @param argName The name of the array argument to be used in building the 
     * {@link IllegalArgumentException} message if the argument failed validation.
     * @return The array argument.
     */
    public static <T> T[] requireNoNullElements(
            @Nullable T[] arg, 
            String argName
    ) {
        if (arg == null) {
            throw new IllegalArgumentException(argName + " must not be null.");
        }
        for (T element : arg) {
            if (element == null) {
                throw new IllegalArgumentException(argName + " must not have null elements.");
            }
        }
        return arg;
    }

    /**
     * Require {@link Collection} argument to have no {@code null} elements.
     * 
     * @param <T> The type of the collection argument.
     * @param arg The {@link Collection} argument.
     * @param argName The name of the {@link Collection} argument to be used in building 
     * the {@link IllegalArgumentException} message if the argument failed validation.
     * @return The {@link Collection} argument.
     */
    public static <T> Collection<T> requireNoNullElements(
            @Nullable Collection<T> arg, 
            String argName
    ) {
        if (arg == null) {
            throw new IllegalArgumentException(argName + " must not be null.");
        }
        for (T element : arg) {
            if (element == null) {
                throw new IllegalArgumentException(argName + " must not have null elements.");
            }
        }
        return arg;
    }
}
