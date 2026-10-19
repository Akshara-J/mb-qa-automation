package io.mb.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Reads config.properties; any key can be overridden with -Dkey=value. */
public final class Config {

    private static final Properties FILE = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                FILE.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read config.properties", e);
        }
    }

    private Config() {
    }

    public static String get(String key, String defaultValue) {
        String fromSystem = System.getProperty(key);
        if (fromSystem != null && !fromSystem.isBlank()) {
            return fromSystem.trim();
        }
        return FILE.getProperty(key, defaultValue).trim();
    }

    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }

    public static String baseUrl() {
        return get("base.url", "https://mb.io/en-AE");
    }

    public static String browser() {
        return get("browser", "chrome");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "true"));
    }

    public static int timeoutSeconds() {
        return getInt("timeout.seconds", 20);
    }

    public static int pageLoadTimeoutSeconds() {
        return getInt("pageload.timeout.seconds", 90);
    }

    public static List<String> skippedLinkDomains() {
        return Arrays.stream(get("linkcheck.skip.domains", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** Turns "/en/explore" into "https://mb.io/en/explore"; absolute URLs pass through untouched. */
    public static String resolve(String pathOrUrl) {
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return pathOrUrl;
        }
        URI base = URI.create(baseUrl());
        String origin = base.getScheme() + "://" + base.getAuthority();
        return origin + (pathOrUrl.startsWith("/") ? pathOrUrl : "/" + pathOrUrl);
    }
}
