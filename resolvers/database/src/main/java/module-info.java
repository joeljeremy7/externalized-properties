module io.github.joeljeremy7.externalizedproperties.resolvers.database {
    exports io.github.joeljeremy7.externalizedproperties.resolvers.database;
    exports io.github.joeljeremy7.externalizedproperties.resolvers.database.queryexecutors;

    requires transitive io.github.joeljeremy7.externalizedproperties.core;
    requires transitive java.sql;
    requires static org.checkerframework.checker.qual;
}
