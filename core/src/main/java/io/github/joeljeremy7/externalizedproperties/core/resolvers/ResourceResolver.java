package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * A {@link Resolver} implementation which reads properties from a resource as represented by
 * a {@link URL}/{@link URI}/{@link Path}. The resource contents may be of any format as long
 * as an implementation of {@link ResourceReader} that can read/parse the resource's contents into a
 * {@link Map} is provided.
 * 
 * <p>For example, given a {@link URL} which resolves to a YAML file e.g. 
 * {@code new URL("file:///path/to/properties.yaml")}, a corresponding {@link ResourceReader}
 * implementation should be provided which uses a YAML library to parse and convert the 
 * {@link URL} resource's contents (in this case a YAML file) to a {@link Map}:
 * 
 * <blockquote><pre>
 * UrlResolver urlResolver = new UrlResolver(
 *     getClass().getResource("/properties.yaml"), 
 *     new YamlReader()
 * );
 * </pre></blockquote>
 */
public class ResourceResolver extends MapResolver {
    /**
     * Constructor. 
     * 
     * <p>Example:</p>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = new ResourceResolver(
     *     getClass().getResource("/path/to/app.properties")
     * );
     * </pre></blockquote>
     * 
     * <p>This expects the contents of {@link URL} resource to be in properties 
     * file format. If the contents are of another format e.g. YAML, JSON, XML, please 
     * provide a custom {@link ResourceReader} via the {@link #ResourceResolver(URL, ResourceReader)}
     * constructor e.g.</p>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = new ResourceResolver(
     *     getClass().getResource("/path/to/properties.yaml"), 
     *     new YamlReader()
     * );
     * </pre></blockquote>
     * 
     * @param url The URL resource to read the properties from.
     */
    private ResourceResolver(URL url) {
        this(url, new PropertiesReader());
    }

    /**
     * Constructor.
     * 
     * @param url The URL resource to read the properties from.
     * @param reader The reader which reads/parses properties from the URL into
     * a {@link Map} instance.
     */
    private ResourceResolver(URL url, ResourceReader reader) {
        super(readMapFromUrl(
            requireNonNull(url, "url"), 
            requireNonNull(reader, "reader")
        ));
    }

    /**
     * Create a {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link URL}.
     * 
     * <p>Example:</p>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = ResourceResolver.fromUrl(
     *     getClass().getResource("/path/to/app.properties")
     * );
     * </pre></blockquote>
     * 
     * <p>This expects the contents of {@link URL} resource to be in properties 
     * file format. If the contents are of another format e.g. YAML, JSON, XML, please 
     * provide a custom {@link ResourceReader} by using the {@link #fromUrl(URL, ResourceReader)} 
     * factory method instead.</p>
     * 
     * @see #fromUrl(URL, ResourceReader)
     * 
     * @param url The URL resource to read properties from.
     * @return The {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link URL}.
     */
    public static ResourceResolver fromUrl(URL url) {
        return new ResourceResolver(url);
    }

    /**
     * Create a {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link URL}.
     * 
     * <p>Examples:</p>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = ResourceResolver.fromUrl(
     *     getClass().getResource("/path/to/properties.yaml"),
     *     new YamlReader()
     * );
     * </pre></blockquote>
     * 
     * <blockquote><pre>
     * public class YamlReader implements ResourceReader {
     *     {@code // Example uses Jackson's ObjectMapper.}
     *     private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
     * 
     *     {@code @Override}
     *     public {@code Map<String, Object>} read(String resourceContents) throws IOException {
     *         return yamlMapper.readValue(
     *             resourceContents, 
     *             new TypeReference{@code <Map<String, Object>>}(){}
     *         );
     *     }
     * }
     * </pre></blockquote>
     * 
     * @param url The URL resource to read properties from.
     * @param reader The reader which reads/parses properties from the URL resource into
     * a {@link Map} instance.
     * @return The {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link URL}.
     */
    public static ResourceResolver fromUrl(URL url, ResourceReader reader) {
        return new ResourceResolver(url, reader);
    }

    /**
     * Create a {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link URI}.
     * 
     * <p> Examples: </p>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = ResourceResolver.fromUri(
     *     URI.create("file:///path/to/app.properties")
     * );
     * </pre></blockquote>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = ResourceResolver.fromUri(
     *     new File("/path/to/app.properties").toURI()
     * );
     * </pre></blockquote>
     * 
     * <p>This expects the contents of {@link URI} resource to be in properties 
     * file format. If the contents are of another format e.g. YAML, JSON, XML, please 
     * provide a custom {@link ResourceReader} by using the {@link #fromUri(URI, ResourceReader)} 
     * factory method instead.</p>
     * 
     * @see #fromUri(URI, ResourceReader)
     * 
     * @param uri The URI resource to read properties from.
     * @return The {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link URL}.
     */
    public static ResourceResolver fromUri(URI uri) {
        return fromUrl(toUrl(requireNonNull(uri, "uri")));
    }

    /**
     * Create a {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link URI}.
     * 
     * <p>Examples:</p>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = ResourceResolver.fromUri(
     *     URI.create("file:///path/to/properties.yaml"), 
     *     new YamlReader()
     * );
     * </pre></blockquote>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = ResourceResolver.fromUri(
     *     new File("/path/to/properties.yaml").toURI(), 
     *     new YamlReader()
     * );
     * </pre></blockquote>
     * 
     * <blockquote><pre>
     * public class YamlReader implements ResourceReader {
     *     {@code // Example uses Jackson's ObjectMapper.}
     *     private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
     * 
     *     {@code @Override}
     *     public {@code Map<String, Object>} read(String resourceContents) throws IOException {
     *         return yamlMapper.readValue(
     *             resourceContents, 
     *             new TypeReference{@code <Map<String, Object>>}(){}
     *         );
     *     }
     * }
     * </pre></blockquote>
     * 
     * @param uri The URI resource to read properties from.
     * @param reader The reader which reads/parses properties from the URI resource into
     * a {@link Map} instance.
     * @return The {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link URL}.
     */
    public static ResourceResolver fromUri(URI uri, ResourceReader reader) {
        return fromUrl(toUrl(requireNonNull(uri, "uri")), reader);
    }

    /**
     * Create a {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link Path}.
     * 
     * <p> Examples: </p>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = ResourceResolver.fromPath(
     *     Paths.get("path", "to", "app.properties")
     * );
     * </pre></blockquote>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = ResourceResolver.fromUri(
     *     new File("/path/to/app.properties").toPath()
     * );
     * </pre></blockquote>
     * 
     * <p>This expects the contents of {@link Path} resource to be in properties 
     * file format. If the contents are of another format e.g. YAML, JSON, XML, please 
     * provide a custom {@link ResourceReader} by using the {@link #fromPath(Path, ResourceReader)} 
     * factory method instead.</p>
     * 
     * @see #fromPath(Path, ResourceReader)
     * 
     * @param path The path resource to read properties from.
     * @return The {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link URL}.
     */
    public static ResourceResolver fromPath(Path path) {
        return fromUrl(toUrl(requireNonNull(path, "path")));
    }

    /**
     * Create a {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link Path}.
     * 
     * <p>Examples:</p>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = ResourceResolver.fromUri(
     *     Paths.get("path", "to", "properties.yaml"), 
     *     new YamlReader()
     * );
     * </pre></blockquote>
     * 
     * <blockquote><pre>
     * ResourceResolver resourceResolver = ResourceResolver.fromUri(
     *     new File("/path/to/properties.yaml").toPath(), 
     *     new YamlReader()
     * );
     * </pre></blockquote>
     * 
     * <blockquote><pre>
     * public class YamlReader implements ResourceReader {
     *     {@code // Example uses Jackson's ObjectMapper.}
     *     private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
     * 
     *     {@code @Override}
     *     public {@code Map<String, Object>} read(String resourceContents) throws IOException {
     *         return yamlMapper.readValue(
     *             resourceContents, 
     *             new TypeReference{@code <Map<String, Object>>}(){}
     *         );
     *     }
     * }
     * </pre></blockquote>
     * 
     * @param path The path resource to read properties from.
     * @param reader The reader which reads/parses properties from the URI resource into
     * a {@link Map} instance.
     * @return The {@link ResourceResolver} which reads and resolves properties from the 
     * given {@link Path}.
     */
    public static ResourceResolver fromPath(Path path, ResourceReader reader) {
        return fromUrl(toUrl(requireNonNull(path, "path")), reader);
    }

    private static Map<String, String> readMapFromUrl(URL url, ResourceReader reader) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            String resourceContent = readString(url.openStream());
            // Add the raw resource String as property.
            result.put(url.toString(), resourceContent);
            Map<String, Object> properties = reader.read(resourceContent);
            // Flatten the properties.
            result.putAll(flattenMap(properties));
        } catch (IOException e) {
            throw new ExternalizedPropertiesException(
                String.format(
                    "An exception occurred while trying to read resource: %s",
                    url.toString()
                ),
                e
            );
        }
        return result;
    }

    private static String readString(InputStream resource) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bufferLength;
        while ((bufferLength = resource.read(buffer)) != -1) {
            result.write(buffer, 0, bufferLength);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    private static Map<String, String> flattenMap(Map<String, Object> source) {
		Map<String, String> flattened = new LinkedHashMap<>();
		buildFlattenedMap(flattened, source, null);
		return flattened;
	}

    /**
     * Flatten the source map to a String-based flat map.
     * 
     * <p>Example:</p>
     * 
     * For collections, it will be converted to indexed keys. Given the YAML:
     * <blockquote><pre>
     * my-list-property:
     *   - value1
     *   - value2
     *   - value3
     * </pre></blockquote>
     * 
     * It will be converted to a flat map with the following mapping:
     * 
     * <blockquote><pre>
     * "my-list-property[0]"="value1"
     * "my-list-property[1]"="value2"
     * "my-list-property[2]"="value3"
     * <pre></blockquote>
     * 
     * For maps, it will be converted to nested keys. Given the YAML:
     * <blockquote><pre>
     * my-map-property:
     *   nested:
     *     property: 
     *       item: item-value
     *     name: name-value
     * </pre></blockquote>
     * 
     * It will be converted to a flat map with the following mapping:
     * 
     * <blockquote><pre>
     * "my-map-property.nested.property.item"="item-value"
     * "my-map-property.nested.name"="name-value"
     * </pre></blockquote>
     * 
     * @param result The result map to put flattened entries to.
     * @param source The source map.
     * @param parentPath The parent path to prepend to a key. Can be {@code null}.
     */
	private static void buildFlattenedMap(
            Map<String, String> result, 
            Map<String, Object> source, 
            @Nullable String parentPath
    ) {
        for (Map.Entry<String, Object> e : source.entrySet()) {
            String key = determineKey(e.getKey(), parentPath);
            Object value = e.getValue();

            if (value instanceof String) {
                result.put(key, (String)value);
            }
            else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>)value;
                buildFlattenedMap(result, map, key);
            }
            else if (value instanceof Collection) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>)value;
                buildFlattenedCollection(result, collection, key);
            }
            else {
                // Stringify value or empty.
                result.put(key, (value != null ? value.toString() : ""));
            }
        }
    }

    private static void buildFlattenedCollection(
            Map<String, String> result, 
            Collection<Object> source,
            String key
    ) {
        if (source.isEmpty()) {
            result.put(key, "");
            return;
        }

        int index = 0;
        for (Object item : source) {
            buildFlattenedMap(
                result, 
                Collections.singletonMap("[" + index++ + "]", item), 
                key
            );
        }
    }

    private static String determineKey(
            String key,
            @Nullable String parentPath
    ) {
        if (parentPath != null) {
            // Means this is nested.
            if (key.charAt(0) == '[') {
                return parentPath + key;
            }

            return parentPath + '.' + key;
        }
        return key;
    }

    private static URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static URL toUrl(Path path) {
        return toUrl(path.toUri());
    }

    /**
     * API for reading properties from a resource based on the format supported by the 
     * {@link ResourceReader} implementation.
     */
    public static interface ResourceReader {
        /**
         * Read properties from {@link InputStream} and return a {@link Map}
         * which contains the parsed key/value pairs.
         * 
         * @param resourceContents The contents of the resource to read/parse properties from.
         * @return The {@link Map} which contains the parsed key/value pairs.
         * @throws IOException if an I/O error occurs.
         */
        Map<String, Object> read(String resourceContents) throws IOException;
    }

    /**
     * A {@link ResourceReader} implementation which loads a {@code .properties} file resource 
     * via {@link Properties}.
     */
    public static class PropertiesReader implements ResourceReader {
        /** {@inheritDoc} */
        @Override
        public Map<String, Object> read(String resourceContents) throws IOException {
            Properties properties = new Properties();
            properties.load(new StringReader(resourceContents));
            return toMap(properties);
        }

        private static Map<String, Object> toMap(Properties properties) {
            return properties.entrySet()
                .stream()
                .filter(e -> 
                    // Ignore non-String keys.
                    e.getKey() instanceof String
                )
                .collect(Collectors.toMap(
                    e -> (String)e.getKey(), 
                    Map.Entry::getValue
                ));
        }
    }
}
