package io.mb.qa.components;

/** What we learned about a marketing image: was it found, did it load, and where does it sit? */
public record BannerInfo(boolean found, boolean loaded, boolean insideBodyRegion, boolean insideHeaderOrFooter) {
}
