package io.mb.qa.utils;

import java.util.Set;

/** Result of probing one link. status = -1 means the request itself failed (DNS, timeout, TLS...). */
public record LinkResult(String url, int status, String finalUrl, String error) {

    /** Typical "bots not welcome" answers - reported as warnings, not failures. */
    private static final Set<Integer> RESTRICTED = Set.of(401, 403, 429, 999);

    public boolean isRestricted() {
        return RESTRICTED.contains(status);
    }

    public boolean isBroken() {
        return status < 0 || (status >= 400 && !isRestricted());
    }

    public String describe() {
        return error != null ? url + " -> " + error : url + " -> HTTP " + status;
    }
}
