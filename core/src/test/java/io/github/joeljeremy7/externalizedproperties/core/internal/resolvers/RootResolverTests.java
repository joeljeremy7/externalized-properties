package io.github.joeljeremy7.externalizedproperties.core.internal.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.Decrypt;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.Decryptor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.MapResolver;
import io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.NoSuchPaddingException;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils.AES_GCM_ALGORITHM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RootResolverTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when resolver provider argument is null")
        void test1() {
            RootProcessor rootProcessor = new RootProcessor();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootResolver(
                    null,
                    rootProcessor
                )
            );
        }

        @Test
        @DisplayName("should throw when root processor provider argument is null")
        void test2() {
            List<Resolver> resolvers = Arrays.asList(new DefaultResolver());

            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootResolver(
                    resolvers,
                    null
                )
            );
        }
    }

    @Nested
    class ResolveMethod {
        // @Test
        // @DisplayName("should throw when proxy method argument is null")
        // void test1() {
        //     RootResolver resolver = rootResolver(Arrays.asList(new DefaultResolver()));
            
        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> resolver.resolve(null, "property")
        //     );
        // }

        // @Test
        // @DisplayName("should throw when property name is null")
        // void test2() {
        //     List<Resolver> resolvers = Arrays.asList(
        //         new DefaultResolver()
        //     );

        //     RootResolver resolver = rootResolver(resolvers);

        //     InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
        //         ProxyInterface::property,
        //         externalizedProperties(resolvers)
        //     );
            
        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> resolver.resolve(context, null)
        //     );
        // }

        @Test
        @DisplayName("should resolve properties from registered resolvers")
        void test1() {
            Map<String, String> propertySource = new HashMap<>();
            propertySource.put("property", "property-value");
            
            List<Resolver> resolvers = Arrays.asList(
                new MapResolver(propertySource)
            );

            RootResolver resolver = rootResolver(resolvers);
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolvers)
            );
            
            Optional<String> result = 
                resolver.resolve(context, "property");

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                propertySource.get("property"), 
                result.get()
            );
        }

        @Test
        @DisplayName("should process resolved properties via registered processors")
        void test2() {
            String originalPropertyValue = "property-value";
            String base64EncodedPropertyValue = EncryptionUtils.encryptAesBase64(
                originalPropertyValue
            );
            Map<String, String> propertySource = new HashMap<>();
            propertySource.put("test.decrypt", base64EncodedPropertyValue);

            List<Resolver> resolvers = Arrays.asList(
                new MapResolver(propertySource)
            );
            Processor processor = new DecryptProcessor(getAesDecryptor());
            
            RootResolver resolver = rootResolver(resolvers, processor);
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::propertyDecrypt,
                externalizedProperties(resolvers, processor)
            );
            
            Optional<String> result = 
                resolver.resolve(context, "test.decrypt");

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(originalPropertyValue, result.get());
        }
    }

    private static RootResolver rootResolver(
            Collection<Resolver> resolvers,
            Processor... processors
    ) {
        return rootResolver(
            resolvers,
            new RootProcessor(Arrays.asList(processors))
        );
    }

    private static RootResolver rootResolver(
            Collection<Resolver> resolvers,
            RootProcessor rootProcessor
    ) {
        return new RootResolver(resolvers, rootProcessor);
    }
    
    private static ExternalizedProperties externalizedProperties(
            Collection<Resolver> resolvers,
            Processor... processors
    ) {
        return ExternalizedProperties.builder()
            .resolvers(resolvers.toArray(Resolver[]::new))
            .processors(processors)
            .build();
    }

    private static Decryptor getAesDecryptor() {
        try {
            return JceDecryptor.factory().symmetric(
                EncryptionUtils.AES_GCM_ALGORITHM, 
                EncryptionUtils.DEFAULT_AES_SECRET_KEY,
                EncryptionUtils.DEFAULT_GCM_PARAMETER_SPEC 
            );
        } catch (InvalidKeyException | 
                NoSuchAlgorithmException | 
                NoSuchPaddingException | 
                InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Cannot instantiate decryptor.", e);
        }
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty("${property}")
        String propertyVariable();

        @ExternalizedProperty("test.decrypt")
        @Decrypt(AES_GCM_ALGORITHM)
        String propertyDecrypt();
    }
}
