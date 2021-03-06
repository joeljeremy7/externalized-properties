package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.internal.InvocationContextFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.InvocationHandlerFactory;

/**
 * The factory for {@link ExternalizedPropertiesInvocationHandler}.
 */
public class ExternalizedPropertiesInvocationHandlerFactory 
        implements InvocationHandlerFactory {

    /** {@inheritDoc} */
    @Override
    public ExternalizedPropertiesInvocationHandler create(
            Class<?> proxyInterface,
            Resolver rootResolver,
            Converter<?> rootConverter,
            VariableExpander variableExpander,
            InvocationContextFactory invocationContextFactory
    ) {
        return new ExternalizedPropertiesInvocationHandler(
            rootResolver,
            rootConverter,
            variableExpander,
            invocationContextFactory
        );
    }
    
}
