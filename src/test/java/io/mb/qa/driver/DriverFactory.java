package io.mb.qa.driver;

import io.mb.qa.config.Config;
import java.time.Duration;
import java.util.Locale;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.Chrome;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/** Creates a browser. Selenium Manager (bundled in Selenium 4.6+) downloads the matching driver binary. */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create(String browser, boolean headless) {
        WebDriver driver = switch (browser.toLowerCase(Locale.ROOT)) {
            case "chrome" -> {
                ChromeOptions o = new ChromeOptions();
                o.addArguments("--lang=en-US", "--no-sandbox", "--disable-dev-shm-usage");
                if (headless) {
                    o.addArguments("--headless=new");
                }
                yield new ChromeDriver(o);
            }
            case "edge" -> {
                EdgeOptions o = new EdgeOptions();
                o.addArguments("--lang=en-US", "--no-sandbox", "--disable-dev-shm-usage");
                if (headless) {
                    o.addArguments("--headless=new");
                }
                yield new EdgeDriver(o);
            }
            case "firefox" -> {
                FirefoxOptions o = new FirefoxOptions();
                if (headless) {
                    o.addArguments("-headless");
                }
                yield new FirefoxDriver(o);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported browser '" + browser + "'. Use chrome, firefox or edge.");
        };
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Config.pageLoadTimeoutSeconds()));
        return driver;
    }

    public static boolean isChromium(String browser) {
        String b = browser.toLowerCase(Locale.ROOT);
        return b.equals("chrome") || b.equals("edge");
    }
}
