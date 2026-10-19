package io.mb.qa.pages;

import io.mb.qa.components.BannerComponent;
import io.mb.qa.components.MarketList;
import io.mb.qa.config.Config;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HomePage extends BasePage {

    private static final By HERO_HEADING = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][contains(normalize-space(), 'Crypto for everyone')]");
    private static final By DOWNLOAD_APP_LINK = By.cssSelector("a[href*='mbio.go.link']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        open(Config.baseUrl());
    }

    public boolean isHeroVisible(Duration timeout) {
        return isVisible(HERO_HEADING, timeout);
    }

    public MarketList marketList(String heading) {
        return new MarketList(driver, heading);
    }

    public BannerComponent banners() {
        return new BannerComponent(driver);
    }

    public boolean isDownloadAppLinkVisible() {
        return isVisible(DOWNLOAD_APP_LINK, Duration.ofSeconds(Config.timeoutSeconds()));
    }

    /** href of the first "Download the app" smart link (go.link redirects to the right store per device). */
    public String downloadAppHref() {
        WebElement link = wait.until(d -> d.findElements(DOWNLOAD_APP_LINK).stream().findFirst().orElse(null));
        return link.getAttribute("href");
    }

    /** All unique http(s) hrefs on the page, fragments stripped. */
    @SuppressWarnings("unchecked")
    public List<String> allHttpLinks() {
        List<String> hrefs = (List<String>) ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('a[href]')).map(a => a.href);");
        return hrefs.stream()
                .filter(h -> h.startsWith("http"))
                .map(h -> h.split("#")[0])
                .filter(h -> !h.isEmpty())
                .distinct()
                .toList();
    }
}
