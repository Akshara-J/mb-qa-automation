package io.mb.qa.pages;

import io.mb.qa.config.Config;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** About Us > Why MultiBank. */
public class WhyMultiBankPage extends BasePage {

    public WhyMultiBankPage(WebDriver driver) {
        super(driver);
    }

    /** Follows the menu/footer link with this text; falls back to the configured path. */
    public void navigateFromCurrentPage(String linkText) {
        List<WebElement> links = driver.findElements(
                By.xpath("//a[contains(normalize-space(.), '" + linkText + "')]"));
        if (links.isEmpty()) {
            open(Config.get("why.multibank.path", "/en/about/why-multibank"));
        } else {
            open(links.get(0).getAttribute("href"));
        }
    }

    public boolean hasHeading(String text) {
        By heading = By.xpath("//*[self::h1 or self::h2 or self::h3 or self::h4]"
                + "[contains(normalize-space(), '" + text + "')]");
        return isVisible(heading, java.time.Duration.ofSeconds(Config.timeoutSeconds()));
    }

    public boolean containsText(String text) {
        return bodyText().replaceAll("\\s+", " ").toLowerCase().contains(text.toLowerCase());
    }
}
