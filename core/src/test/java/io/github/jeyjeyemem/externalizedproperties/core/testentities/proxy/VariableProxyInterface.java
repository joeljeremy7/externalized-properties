package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

public interface VariableProxyInterface {
    @ExternalizedProperty("property-${custom.variable}")
    String variableProperty();

    @ExternalizedProperty("custom.variable")
    String customVariable();
}
