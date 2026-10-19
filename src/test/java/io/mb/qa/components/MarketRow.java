package io.mb.qa.components;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One trading pair entry. We validate by *content shape* (a name, a price, a % change) rather than
 * by CSS class, so the checks survive front-end refactors.
 */
public record MarketRow(String text) {

    private static final Pattern NAME = Pattern.compile("[A-Za-z]{2,}");
    private static final Pattern PRICE = Pattern.compile(
            "[$€£]\\s?\\d[\\d,]*(\\.\\d+)?|\\b\\d[\\d,]*\\.\\d+\\b(?!\\s?%)");
    private static final Pattern CHANGE = Pattern.compile("([+\\-−–]?)\\s?(\\d+(?:\\.\\d+)?)\\s?%");

    public boolean hasName() {
        return NAME.matcher(text).find();
    }

    public boolean hasPrice() {
        return PRICE.matcher(text).find();
    }

    /** Signed 24h change in percent, or empty if the row shows none. */
    public Optional<Double> changePercent() {
        Matcher m = CHANGE.matcher(text);
        if (!m.find()) {
            return Optional.empty();
        }
        double value = Double.parseDouble(m.group(2));
        String sign = m.group(1);
        boolean negative = sign.equals("-") || sign.equals("−") || sign.equals("–")
                || text.contains("▼") || text.contains("↓") || text.contains("▾");
        return Optional.of(negative ? -value : value);
    }

    public String oneLine() {
        return text.replaceAll("\\s+", " ").trim();
    }
}
