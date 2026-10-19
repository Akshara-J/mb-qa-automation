package io.mb.qa.components;

import io.mb.qa.config.Config;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * A list of trading pairs sitting under a heading (e.g. "Top Gainers", "Spot market").
 * The section is found by its heading text; rows are table rows, or - if the site renders cards -
 * links into /explore/. These two selectors are the only place to touch if the markup changes.
 */
public class MarketList {

    private static final By TABLE_ROWS = By.cssSelector("tbody tr");
    private static final By CARD_ROWS = By.cssSelector("a[href*='/explore/']");

    private final WebDriver driver;
    private final String heading;
    private final By sectionLocator;

    public MarketList(WebDriver driver, String heading) {
        this.driver = driver;
        this.heading = heading;
        // nearest ancestor of the heading that actually contains rows
        this.sectionLocator = By.xpath(
                "//*[self::h1 or self::h2 or self::h3 or self::h4 or self::h5][contains(normalize-space(), '"
                        + heading + "')]/ancestor::*[.//tbody//tr or .//a[contains(@href,'/explore/')]][1]");
    }

    /** Waits for market data (it is loaded asynchronously) and returns the visible rows. */
    public List<MarketRow> rows() {
        FluentWait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(Config.timeoutSeconds()))
                .ignoring(StaleElementReferenceException.class);
        try {
            return wait.until(d -> {
                List<WebElement> section = d.findElements(sectionLocator);
                if (section.isEmpty()) {
                    return null;
                }
                List<WebElement> els = section.get(0).findElements(TABLE_ROWS);
                if (els.isEmpty()) {
                    els = section.get(0).findElements(CARD_ROWS);
                }
                List<MarketRow> rows = els.stream()
                        .filter(WebElement::isDisplayed)
                        .map(e -> new MarketRow(e.getText().trim()))
                        .filter(r -> !r.text().isEmpty())
                        .toList();
                return rows.isEmpty() ? null : rows;
            });
        } catch (TimeoutException e) {
            throw new AssertionError("Section '" + heading + "' did not render any trading pairs within "
                    + Config.timeoutSeconds() + "s", e);
        }
    }
}
