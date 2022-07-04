# Externalized Properties

[![Gradle Build](https://github.com/joeljeremy7/externalized-properties/actions/workflows/gradle-build.yaml/badge.svg)](https://github.com/joeljeremy7/externalized-properties/actions/workflows/gradle-build.yaml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.joeljeremy7.externalizedproperties/core/badge.svg)](https://search.maven.org/search?q=g:%22io.github.joeljeremy7.externalizedproperties%22)
[![Coverage Status](https://coveralls.io/repos/github/joeljeremy7/externalized-properties/badge.svg?branch=main)](https://coveralls.io/github/joeljeremy7/externalized-properties?branch=main)
[![Known Vulnerabilities](https://snyk.io/test/github/joeljeremy7/externalized-properties/badge.svg)](https://snyk.io/test/github/joeljeremy7/externalized-properties)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/joeljeremy7/externalized-properties/blob/main/LICENSE)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/joeljeremy7/externalized-properties.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/joeljeremy7/externalized-properties/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/joeljeremy7/externalized-properties.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/joeljeremy7/externalized-properties/context:java)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=io.github.joeljeremy7.externalizedproperties&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=io.github.joeljeremy7.externalizedproperties)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joeljeremy7.externalizedproperties&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joeljeremy7.externalizedproperties)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joeljeremy7.externalizedproperties&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joeljeremy7.externalizedproperties)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.joeljeremy7.externalizedproperties&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=io.github.joeljeremy7.externalizedproperties)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=io.github.joeljeremy7.externalizedproperties&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=io.github.joeljeremy7.externalizedproperties)

A lightweight and extensible library to resolve application properties from various external sources.

## [Twelve Factor Methodology](https://12factor.net)

Externalized Properties was inspired by the [The Twelve Factor Methodology](https://12factor.net)'s section [III. Config](https://12factor.net/config).  

The goal of this library is to make it easy for applications to implement configuration best practices by providing easy-to-use APIs as well as providing the flexibility to choose where to store their configurations/properties.

## 🛠️ Installation

### Gradle

```groovy
implementation "io.github.joeljeremy7.externalizedproperties:core:${version}"
```

### Maven

```xml
<dependency>
    <groupId>io.github.joeljeremy7.externalizedproperties</groupId>
    <artifactId>core</artifactId>
    <version>${version}</version>
</dependency>
```

### 🧩 JPMS Module Names

- Core - `io.github.joeljeremy7.externalizedproperties.core`
- Database Resolver - `io.github.joeljeremy7.externalizedproperties.resolvers.database`
- Git Resolver - `io.github.joeljeremy7.externalizedproperties.resolvers.git`

Module authors can use above module names in their module-info.java:

```java
module foo.bar {
    requires io.github.joeljeremy7.externalizedproperties.core;
    requires io.github.joeljeremy7.externalizedproperties.resolvers.database;
    requires io.github.joeljeremy7.externalizedproperties.resolvers.git;
}
```

## 🌟 Features

### 🔗 [Property Resolution via Java Dynamic Proxies](docs/property-resolution.md) ([Why Dynamic Proxies?](docs/why-dynamic-proxies.md))

✨ Proxy Interface Property Mapping (via [@ExternalizedProperty](core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperty.java))  
✨ Default/Fallback Values  
✨ Non-Static/Dynamic Property Names (via [@ResolverFacade](core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ResolverFacade.java))  
✨ Support for Various Configuration File/Resource Formats  
✨ Caching  
✨ Eager Loading  
✨ Automatic Cache Reload  

### 🔗 [Conversion](docs/conversion.md)

✨ Automatic Property Conversion  
✨ Generic Type Conversion  
✨ Conversion of Arbitrary Values (via [@ConverterFacade](core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ConverterFacade.java))

### 🔗 [Variable Expansion](docs/variable-expansion.md)

✨ Automatic Variable Expansion in Property Names  
✨ Automatic Variable Expansion in Properties  
✨ Variable Expansion in Arbitrary Values (via [@VariableExpanderFacade](core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/VariableExpanderFacade.java))

### 🔗 [Processing](docs/processing.md)

✨ Targeted Processing

### 🔗 [Profiles](docs/profiles.md)

✨ Profile-Specific Configurations

### 🔗 [Ordinal Components](docs/ordinal-components.md)

✨ Ordinal Resolvers  
✨ Ordinal Converters

## 🏎️ Quick Start

Properties are mapped to proxy interface methods by using the [@ExternalizedProperty](core/src/main/java/io/github/joeljeremy7/externalizedproperties/core/ExternalizedProperty.java) annotation.

(For more advanced scenarios, please see the feature documentations.)

Given an interface:

```java
public interface ApplicationProperties {
    @ExternalizedProperty("java.home")
    String javaHome();

    @ExternalizedProperty("encrypted.property")
    @Decrypt("MyDecryptor")
    String encryptedProperty();

    @ExternalizedProperty("java.version")
    int javaVersion();
}
```

We can initialize and start resolving external configurations/properties by:

```java
public static void main(String[] args) {
    // 1. Configure and build the ExternalizedProperties instance.
    ExternalizedProperties externalizedProperties = buildExternalizedProperties();

    // 2. Initialize a proxy using the ExternalizedProperties.
    ApplicationProperties props = externalizedProperties.initialize(ApplicationProperties.class);

    // 3. Resolve the properties.
    String javaHome = props.javaHome();
    String encryptedProperty = props.encryptedProperty();
    int javaVersion = props.javaVersion();
}

private static ExternalizedProperties buildExternalizedProperties() {
    return ExternalizedProperties.builder()
        .defaults() 
        .resolvers(...)
        .processors(...)
        .converters(...) 
        .build();
}
```

## 🧪 Samples

More sample can be found here: <https://github.com/joeljeremy7/externalized-properties-samples>
