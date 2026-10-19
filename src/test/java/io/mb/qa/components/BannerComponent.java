package io.mb.qa.components;

import io.mb.qa.config.Config;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Inspects marketing images. "Expected region" = between the header and the footer,
 * not inside either of them, and actually loaded (naturalWidth > 0, i.e. not a broken image).
 */
public class BannerComponent {

    private static final String INSPECT_SCRIPT =
            "const img = arguments[0];"
                    + "const header = document.querySelector('header,[role=banner]');"
                    + "const footer = document.querySelector('footer');"
                    + "const r = img.getBoundingClientRect();"
                    + "const top = r.top + window.scrollY, bottom = r.bottom + window.scrollY;"
                    + "const headerBottom = header ? header.getBoundingClientRect().bottom + window.scrollY : 0;"
                    + "const footerTop = footer ? footer.getBoundingClientRect().top + window.scrollY : Infinity;"
                    + "return {loaded: img.complete && img.naturalWidth > 0,"
                    + "        inRegion: top >= headerBottom - 1 && bottom <= footerTop + 1,"
                    + "        inHeaderOrFooter: !!img.closest('header,footer')};";

    private final WebDriver driver;

    public BannerComponent(WebDriver driver) {
        this.driver = driver;
    }

    @SuppressWarnings("unchecked")
    public BannerInfo inspect(String srcFragment) {
        By locator = By.cssSelector("img[src*='" + srcFragment + "'], img[srcset*='" + srcFragment + "']");
        // Responsive sites ship several variants of one banner; the visible one is the one that counts.
        WebElement visible = null;
        try {
            visible = new WebDriverWait(driver, Duration.ofSeconds(Config.timeoutSeconds())).until(d -> {
                List<WebElement> candidates = d.findElements(locator);
                for (WebElement img : candidates) {
                    ((JavascriptExecutor) d).executeScript("arguments[0].scrollIntoView({block:'center'});", img);
                    if (img.isDisplayed()) {
                        return img;
                    }
                }
                return null;
            });
        } catch (TimeoutException e) {
            return new BannerInfo(false, false, false, false);
        }
        final WebElement banner = visible;
        try { // lazy-loaded images need a moment after scrolling into view
            new WebDriverWait(driver, Duration.ofSeconds(Config.timeoutSeconds())).until(d -> {
                Map<String, Object> m = (Map<String, Object>) ((JavascriptExecutor) d)
                        .executeScript(INSPECT_SCRIPT, banner);
                return Boolean.TRUE.equals(m.get("loaded"));
            });
        } catch (TimeoutException ignored) {
            // fall through: the final inspection below reports loaded=false
        }
        Map<String, Object> m = (Map<String, Object>) ((JavascriptExecutor) driver)
                .executeScript(INSPECT_SCRIPT, banner);
        return new BannerInfo(true,
                Boolean.TRUE.equals(m.get("loaded")),
                Boolean.TRUE.equals(m.get("inRegion")),
                Boolean.TRUE.equals(m.get("inHeaderOrFooter")));
    }
}
