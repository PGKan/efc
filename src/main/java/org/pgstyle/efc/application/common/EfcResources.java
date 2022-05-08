package org.pgstyle.efc.application.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.pgstyle.efc.application.ApplicationException;

/**
 * Resource handler class, it loads and stores resources from Jar at
 * initialisation.
 *
 * @version 1.0
 * @author PGKan
 */
public final class EfcResources {

    /** ${name} placeholder  */
    private static final Pattern PLACEHOLDER;
    /** $${name} escaped placeholder  */
    private static final Pattern ESCAPED_PLACEHOLDER;
    /** loaded resources entry */
    private static final Properties resources;
    /** loaded binary resources */
    private static final Map<String, byte[]> binaries;

    static {
        PLACEHOLDER = Pattern.compile("(?<!\\$)\\$\\{[^\\$\\{\\}]+\\}");
        ESCAPED_PLACEHOLDER = Pattern.compile("\\$\\$\\{[^\\$\\{\\}]+\\}");
        binaries = new HashMap<>();
        resources = EfcResources.loadResources();
    }

    /**
     * Returns {@code true} if the specified resource key exists.
     *
     * @param key the resource key to be tested
     * @return {@code true} if the specified resource key exists; or
     *         {@code false} otherwise
     */
    public static boolean exist(String key) {
        return EfcResources.resources.containsKey(key);
    }

    /**
     * Returns the resource content in string.
     *
     * @param key the resource key
     * @return the resource content in string
     */
    public static String get(String key) {
        return EfcResources.exist(key) ? EfcResources.resources.getProperty(key) : ("missing resource " + key);
    }

    /**
     * Returns the resource content in string.
     *
     * @param key the resource key
     * @return the resource content in string
     */
    public static InputStream getStream(String key) {
        String uuid = EfcResources.resources.getProperty(key);
        return EfcResources.binaries.containsKey(uuid) ? new ByteArrayInputStream(EfcResources.binaries.get(uuid)) : null;
    }

    /**
     * Loads Jar resources.
     *
     * @return loaded resources entry
     * @throws ApplicationException if failed to load the resource entries
     */
    private static Properties loadResources() {
        try {
            Properties resources = new Properties();
            resources.load(new StringReader(EfcUtils.loadResourceAsString("/org.pgstyle/efc/resources")));
            EfcResources.resolve(resources);
            resources.forEach((k, v) -> resources.put(k, EfcResources.binary(k.toString(), v.toString())));
            EfcResources.resolve(resources);
            resources.forEach((k, v) -> resources.put(k, EfcResources.extern(k.toString(), v.toString())));
            EfcResources.resolve(resources);
            resources.forEach((k, v) -> resources.put(k, EfcResources.descape(v.toString())));
            return resources;
        } catch (IOException | IllegalArgumentException e) {
            throw new ApplicationException("fail to load resource entries", e);
        }
    }

    /**
     * Descapes escaped placeholders in string
     *
     * @param value the string to be descaped
     * @return the descaped string
     */
    private static String descape(String value) {
        Matcher placeholders = EfcResources.PLACEHOLDER.matcher(value);
        while (placeholders.find()) {
            String source = placeholders.group();
            value = value.replace(source, "");
        }
        Matcher escaped = EfcResources.ESCAPED_PLACEHOLDER.matcher(value);
        while (escaped.find()) {
            String source = escaped.group();
            value = value.replace(source, source.substring(1));
        }
        return value;
    }

    /**
     * Resolves external resource text.
     *
     * @param key the key of the resource entry to be resolved
     * @param value the value of the resource entry to be resolved
     * @return the resolved resource value
     * @throws ApplicationException if failed to load external resource
     */
    private static String extern(String key, String value) {
        try {
            return Optional.ofNullable(value).filter(v -> v.startsWith("&extern ")).map(v -> EfcResources.loadExtern(v.substring(8))).orElse(value);
        }
        catch (RuntimeException e) {
            throw new ApplicationException("fail to load extern resource: " + key, e);
        }
    }

    /**
     * Resolves binary resource.
     *
     * @param key the key of the resource entry to be resolved
     * @param value the value of the resource entry to be resolved
     * @return resolved binary resource token
     * @throws ApplicationException if failed to load external resource
     */
    private static String binary(String key, String value) {
        try {
            return Optional.ofNullable(value).filter(v -> v.startsWith("&binary ")).map(v -> {
                String uuid = UUID.randomUUID().toString();
                String path = v.substring(8);
                try {
                    EfcResources.binaries.put(uuid, EfcUtils.read(EfcUtils.class.getResourceAsStream("/" + path)));
                } catch (IOException e) {
                    throw new ApplicationException("fail to load resource: " + path, e);
                }
                return uuid;
            }).orElse(value);
        }
        catch (RuntimeException e) {
            throw new ApplicationException("fail to load binary resource: " + key, e);
        }
    }

    /**
     * Loads external resource contents in Jar.
     *
     * @param string the paths of external resources
     * @return the loaded resource content
     */
    private static String loadExtern(String string) {
        return Arrays.stream(string.split("(?<!\\\\)[ ;]+")).filter(s -> !s.isEmpty())
            .map(p -> EfcResources.loadFromResource("/" + p)).collect(Collectors.joining());
    }

    /**
     * Loads resource from file in Jar as string.
     *
     * @param path the path of the resource file
     * @return the loaded file contents
     * @throws ApplicationException if failed to load the resource
     */
    private static String loadFromResource(String path) {
        try {
            return EfcUtils.loadResourceAsString(path);
        } catch (IOException | IllegalArgumentException e) {
            throw new ApplicationException("failed to load Jar resource: " + path, e);
        }
    }

    /**
     * Resolves external and internal references in the properties object.
     *
     * @param properties the properties object to be resolved
     * @return the properties object itself
     */
    private static Properties resolve(Properties properties) {
        boolean resolved = false;
        String identity = properties.toString();
        while (!resolved) {
            properties.forEach((k, v) -> properties.put(k, EfcResources.resolve(properties, v.toString())));
            String newIdentity = properties.toString();
            resolved = identity.equals(newIdentity);
            identity = newIdentity;
        }
        return properties;
    }

    /**
     * Resolves external and internal references in the property entry.
     *
     * @param properties the properties object for internal reference
     * @param value the value of the property entry
     * @return the resolved value
     */
    private static String resolve(Properties properties, String value) {
        Matcher placeholders = EfcResources.PLACEHOLDER.matcher(value);
        while (placeholders.find()) {
            String source = placeholders.group();
            String sourceValue = properties.getProperty(source.substring(2, source.length() - 1), "");
            if (!sourceValue.isEmpty() && !sourceValue.startsWith("&extern ")) {
                value = value.replace(source, sourceValue);
            }
        }
        return value;
    }

    /** Unnewable @throws UnsupportedOperationException always */
    private EfcResources() {
        throw new UnsupportedOperationException("unnewable");
    }

}
