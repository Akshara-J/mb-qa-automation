package io.mb.qa.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.mb.qa.config.Config;
import io.mb.qa.context.TestContext;
import io.mb.qa.driver.DriverFactory;
import org.junit.jupiter.api.Assumptions;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/** Browser lifecycle. A new browser per scenario = no shared state, no order dependence. */
public class Hooks {

    private final TestContext ctx;

    public Hooks(TestContext ctx) {
        this.ctx = ctx;
    }

    /** Scenarios tagged @chromium-only use Chrome DevTools Protocol; skip (not fail) elsewhere. */
    @Before(value = "@chromium-only", order = 0)
    public void skipOnNonChromium() {
        Assumptions.assumeTrue(DriverFactory.isChromium(Config.browser()),
                "Scenario needs Chromium (Chrome/Edge) - skipped on " + Config.browser());
    }

    @Before(order = 10)
    public void startBrowser(Scenario scenario) {
        ctx.setScenario(scenario);
        ctx.setDriver(DriverFactory.create(Config.browser(), Config.headless()));
    }

    @After
    public void stopBrowser(Scenario scenario) {
        try {
            if (scenario.isFailed() && ctx.hasDriver()) {
                byte[] png = ((TakesScreenshot) ctx.driver()).getScreenshotAs(OutputType.BYTES);
                scenario.attach(png, "image/png", "Failure screenshot");
                scenario.log("URL at failure: " + ctx.driver().getCurrentUrl());
            }
        } catch (RuntimeException e) {
            scenario.log("Could not capture screenshot: " + e.getMessage());
        } finally {
            ctx.quit();
        }
    }
}
