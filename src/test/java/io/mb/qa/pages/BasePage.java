package io.mb.qa.pages;

import io.mb.qa.config.Config;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Behaviour shared by every page. No Selenium call is allowed outside pages/components. */
public class BasePage {

    private static final By COOKIE_ACCEPT = By.xpath(
            "//button[normalize-space()='Accept' or normalize-space()='Accept all' or normalize-space()='Accept All'"
                    + " or normalize-space()='Got it' or normalize-space()='I agree']");

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.timeoutSeconds()));
    }

    public void open(String pathOrUrl) {
        driver.get(Config.resolve(pathOrUrl));
        waitForDocumentReady();
        dismissCookieBannerIfPresent();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public String bodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    public void waitForDocumentReady() {
        wait.until(d -> "complete".equals(js("return document.readyState")));
    }

    /** True when the page is wider than the viewport - the classic responsive-layout bug. */
    public boolean hasHorizontalScroll() {
        Object result = js("return document.documentElement.scrollWidth > window.innerWidth + 1;");
        return Boolean.TRUE.equals(result);
    }

    public boolean isVisible(By locator, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(d -> {
                List<WebElement> els = d.findElements(locator);
                return els.stream().anyMatch(WebElement::isDisplayed);
            });
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    protected void scrollIntoView(WebElement element) {
        js("arguments[0].scrollIntoView({block:'center'});", element);
    }

    private void dismissCookieBannerIfPresent() {
        for (WebElement button : driver.findElements(COOKIE_ACCEPT)) {
            try {
                if (button.isDisplayed()) {
                    button.click();
                    return;
                }
            } catch (RuntimeException ignored) {
                // a banner that vanishes mid-click is not a test failure
            }
        }
    }
}
