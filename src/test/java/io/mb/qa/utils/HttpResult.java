package io.mb.qa.utils;

import java.net.URI;

/** Outcome of a plain HTTP GET (after following redirects). */
public record HttpResult(String requestedUrl, String finalUrl, int status, String contentType, String body) {

    public String finalHost() {
        return URI.create(finalUrl).getHost();
    }
}
