package io.mb.qa.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.mb.qa.components.HeaderComponent;
import io.mb.qa.components.NavLink;
import io.mb.qa.config.Config;
import io.mb.qa.context.TestContext;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

public class NavigationSteps {

    private final TestContext ctx;

    public NavigationSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    @Then("the header should be visible")
    public void headerVisible() {
        assertThat(ctx.header().isVisible()).as("header visible").isTrue();
    }

    @Then("the header logo should be visible")
    public void logoVisible() {
        assertThat(ctx.header().isLogoVisible()).as("logo visible").isTrue();
    }

    @Then("the header should contain the following items")
    public void headerContains(DataTable table) {
        HeaderComponent header = ctx.header();
        header.openMobileMenuIfPresent();
        List<NavLink> links = header.visibleLinks();
        SoftAssertions soft = new SoftAssertions();
        for (Map<String, String> row : table.asMaps()) {
            String item = row.get("item");
            soft.assertThat(links.stream().anyMatch(l -> l.label().equalsIgnoreCase(item)))
                    .as("header item '%s' (found: %s)", item, links.stream().map(NavLink::label).toList())
                    .isTrue();
        }
        soft.assertAll();
    }

    @Then("every visible header link should have a destination")
    public void everyLinkHasDestination() {
        List<NavLink> links = ctx.header().visibleLinks();
        assertThat(links).as("header links").isNotEmpty();
        SoftAssertions soft = new SoftAssertions();
        for (NavLink l : links) {
            soft.assertThat(l.href()).as("destination of '%s'", l.label()).isNotBlank();
            soft.assertThat(l.href()).as("destination of '%s'", l.label()).doesNotEndWith("#");
        }
        soft.assertAll();
    }

    @Then("the header link {string} should point to a URL containing {string}")
    public void linkPointsTo(String label, String fragment) {
        NavLink link = ctx.header().findLink(label)
                .orElseThrow(() -> new AssertionError("Header link not found: " + label));
        assertThat(link.href()).as("href of '%s'", label).contains(fragment);
    }

    @When("I click the header link {string}")
    public void clickHeaderLink(String label) {
        int before = ctx.driver().getWindowHandles().size();
        ctx.header().clickLink(label);
        try { // the link may open in a new tab
            new WebDriverWait(ctx.driver(), Duration.ofSeconds(3))
                    .until(d -> d.getWindowHandles().size() > before);
        } catch (TimeoutException ignored) {
            // same-tab navigation
        }
        ctx.switchToLatestWindow();
    }

    @When("I click the header logo")
    public void clickLogo() {
        ctx.header().clickLogo();
    }

    @Then("the browser should land on a URL containing {string}")
    public void landedOn(String fragment) {
        new WebDriverWait(ctx.driver(), Duration.ofSeconds(Config.timeoutSeconds()))
                .until(d -> d.getCurrentUrl().contains(fragment));
        assertThat(ctx.driver().getCurrentUrl()).contains(fragment);
    }

    @Then("I should be on the home page")
    public void onHomePage() {
        new WebDriverWait(ctx.driver(), Duration.ofSeconds(Config.timeoutSeconds()))
                .until(d -> isHome(d.getCurrentUrl()));
        assertThat(isHome(ctx.driver().getCurrentUrl())).as("current URL is the home page").isTrue();
    }

    @Then("the header should offer a mobile menu toggle or visible navigation links")
    public void mobileNavigationAvailable() {
        HeaderComponent header = ctx.header();
        assertThat(header.hasMobileMenuToggle() || !header.visibleLinks().isEmpty())
                .as("a way to navigate exists at this viewport")
                .isTrue();
    }

    private static boolean isHome(String url) {
        URI uri = URI.create(url);
        String path = uri.getPath() == null ? "" : uri.getPath().replaceAll("/+$", "");
        return uri.getHost().endsWith("mb.io") && !uri.getHost().startsWith("trade")
                && Set.of("", "/en", "/en-AE", "/en-ae").contains(path);
    }
}
