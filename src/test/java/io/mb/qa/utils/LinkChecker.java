package io.mb.qa.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Lightweight HTTP client used for "does this URL resolve?" checks - far faster than driving a browser. */
public final class LinkChecker {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private LinkChecker() {
    }

    public static HttpResult get(String url, String userAgent) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .GET()
                .build();
        IOException last = null;
        for (int attempt = 1; attempt <= 2; attempt++) { // one retry absorbs transient network noise
            try {
                HttpResponse<String> r = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                return new HttpResult(url, r.uri().toString(), r.statusCode(),
                        r.headers().firstValue("content-type").orElse(""), r.body());
            } catch (IOException e) {
                last = e;
            }
        }
        throw last;
    }

    public static LinkResult probe(String url) {
        try {
            HttpResult r = get(url, UserAgents.DESKTOP);
            return new LinkResult(url, r.status(), r.finalUrl(), null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new LinkResult(url, -1, url, "Interrupted");
        } catch (Exception e) {
            return new LinkResult(url, -1, url, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public static List<LinkResult> probeAll(Collection<String> urls, int threads) {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<LinkResult>> futures = new ArrayList<>();
            for (String url : urls) {
                futures.add(pool.submit(() -> probe(url)));
            }
            List<LinkResult> results = new ArrayList<>();
            for (Future<LinkResult> f : futures) {
                try {
                    results.add(f.get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Link check interrupted", e);
                } catch (ExecutionException e) {
                    throw new IllegalStateException("Link check failed", e);
                }
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }
}
