package io.mb.qa.context;

import io.cucumber.java.Scenario;
import io.mb.qa.components.HeaderComponent;
import io.mb.qa.pages.ExplorePage;
import io.mb.qa.pages.HomePage;
import io.mb.qa.utils.HttpResult;
import io.mb.qa.utils.LinkResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.WebDriver;

/**
 * Per-scenario state, injected into hooks and step classes by PicoContainer.
 * One fresh instance per scenario keeps scenarios fully independent.
 */
public class TestContext {

    private WebDriver driver;
    private Scenario scenario;

    // state handed from one step to the next inside a scenario
    public HttpResult lastHttpResult;
    public String currentLink;
    public List<String> collectedLinks = new ArrayList<>();
    public List<LinkResult> linkResults = new ArrayList<>();

    public WebDriver driver() {
        if (driver == null) {
            throw new IllegalStateException("Browser not started - did the @Before hook run?");
        }
        return driver;
    }

    public boolean hasDriver() {
        return driver != null;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public void log(String message) {
        if (scenario != null) {
            scenario.log(message);
        }
    }

    public void quit() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (RuntimeException ignored) {
                // browser already gone
            }
            driver = null;
        }
    }

    public HomePage home() {
        return new HomePage(driver());
    }

    public ExplorePage explore() {
        return new ExplorePage(driver());
    }

    public HeaderComponent header() {
        return new HeaderComponent(driver());
    }

    /** Clicking a link may open a new tab; follow it. */
    public void switchToLatestWindow() {
        Set<String> handles = driver().getWindowHandles();
        String last = null;
        for (String h : handles) {
            last = h;
        }
        if (last != null) {
            driver().switchTo().window(last);
        }
    }
}
