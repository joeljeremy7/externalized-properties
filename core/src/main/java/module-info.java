module io.github.joeljeremy7.externalizedproperties.core {
    exports io.github.joeljeremy7.externalizedproperties.core;
    exports io.github.joeljeremy7.externalizedproperties.core.conversion;
    exports io.github.joeljeremy7.externalizedproperties.core.conversion.converters;
    exports io.github.joeljeremy7.externalizedproperties.core.processing;
    exports io.github.joeljeremy7.externalizedproperties.core.processing.processors;
    exports io.github.joeljeremy7.externalizedproperties.core.resolvers;
    exports io.github.joeljeremy7.externalizedproperties.core.variableexpansion;
    exports io.github.joeljeremy7.externalizedproperties.core.internal;

    uses io.github.joeljeremy7.externalizedproperties.core.Resolver;

    requires java.logging;
    requires static org.checkerframework.checker.qual;
}
