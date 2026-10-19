package io.mb.qa.components;

import io.mb.qa.config.Config;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** The top navigation bar. All header locators live here - change them in one place. */
public class HeaderComponent {

    private static final By ROOT = By.cssSelector("header, [role='banner']");
    private static final By ANCHORS = By.cssSelector("a[href]");
    private static final By LOGO_BY_HREF = By.xpath(
            ".//a[@href='/' or @href='https://mb.io/' or @href='https://mb.io' or @href='/en' or @href='/en-AE'"
                    + " or @href='https://mb.io/en']");
    private static final By LOGO_BY_IMAGE = By.xpath(".//a[.//img or .//*[name()='svg']]");
    private static final By MOBILE_TOGGLE = By.cssSelector(
            "button[aria-label*='menu' i], button[aria-controls*='menu' i],"
                    + " button[class*='burger' i], button[class*='hamburger' i]");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public HeaderComponent(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.timeoutSeconds()));
    }

    public boolean isVisible() {
        try {
            wait.until(d -> d.findElements(ROOT).stream().anyMatch(WebElement::isDisplayed));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isLogoVisible() {
        return logo().map(WebElement::isDisplayed).orElse(false);
    }

    public void clickLogo() {
        logo().orElseThrow(() -> new NoSuchElementException("Header logo link not found")).click();
    }

    public boolean hasMobileMenuToggle() {
        return root().findElements(MOBILE_TOGGLE).stream().anyMatch(WebElement::isDisplayed);
    }

    /** On narrow viewports links sit behind a toggle; on desktop this is a no-op. */
    public void openMobileMenuIfPresent() {
        root().findElements(MOBILE_TOGGLE).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .ifPresent(WebElement::click);
    }

    public List<NavLink> visibleLinks() {
        List<NavLink> links = new ArrayList<>();
        for (WebElement a : root().findElements(ANCHORS)) {
            if (a.isDisplayed()) {
                links.add(new NavLink(labelOf(a), a.getAttribute("href")));
            }
        }
        return links;
    }

    public Optional<NavLink> findLink(String label) {
        return visibleLinks().stream().filter(l -> l.label().equalsIgnoreCase(label)).findFirst();
    }

    public void clickLink(String label) {
        WebElement target = root().findElements(ANCHORS).stream()
                .filter(WebElement::isDisplayed)
                .filter(a -> labelOf(a).equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "No visible header link labelled '" + label + "'. Found: " + visibleLinks()));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", target);
        target.click();
    }

    private WebElement root() {
        return driver.findElements(ROOT).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseGet(() -> driver.findElement(ROOT));
    }

    private Optional<WebElement> logo() {
        List<WebElement> byHref = root().findElements(LOGO_BY_HREF);
        if (!byHref.isEmpty()) {
            return Optional.of(byHref.get(0));
        }
        return root().findElements(LOGO_BY_IMAGE).stream().findFirst();
    }

    private static String labelOf(WebElement anchor) {
        String text = anchor.getText().trim();
        if (text.isEmpty()) {
            text = String.valueOf(anchor.getAttribute("aria-label")).trim();
        }
        if (text.isEmpty() || text.equals("null")) {
            text = String.valueOf(anchor.getAttribute("title")).trim();
        }
        return text.isEmpty() || text.equals("null") ? "(icon link)" : text;
    }
}
