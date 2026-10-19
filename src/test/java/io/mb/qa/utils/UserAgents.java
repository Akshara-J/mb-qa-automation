package io.mb.qa.utils;

public final class UserAgents {
    public static final String DESKTOP =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
    public static final String IOS =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1";
    public static final String ANDROID =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36";

    private UserAgents() {
    }

    public static String forPlatform(String platform) {
        return switch (platform.toLowerCase()) {
            case "ios" -> IOS;
            case "android" -> ANDROID;
            case "desktop" -> DESKTOP;
            default -> throw new IllegalArgumentException("Unknown platform: " + platform);
        };
    }
}
