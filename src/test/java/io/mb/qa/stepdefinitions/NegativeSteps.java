package io.mb.qa.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.mb.qa.config.Config;
import io.mb.qa.context.TestContext;
import io.mb.qa.pages.BasePage;
import io.mb.qa.utils.LinkChecker;
import io.mb.qa.utils.LinkResult;
import io.mb.qa.utils.UserAgents;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.openqa.selenium.chromium.HasCdp;

/** Failure-mode steps: bad routes, broken links, hostile networks. */
public class NegativeSteps {

    private static final Pattern NOT_FOUND_TEXT =
            Pattern.compile("(?i)(404|not found|doesn.?t exist|does not exist|page.*(missing|unavailable))");
    private static final Pattern TECHNICAL_LEAK =
            Pattern.compile("(?i)(stack ?trace|exception|internal server error|at java\\.|undefined|ECONNREFUSED)");
    private static final Pattern APP_CRASH =
            Pattern.compile("(?i)(application error|unhandled runtime error|client-side exception|internal server error)");
    private static final Pattern PLACEHOLDER_LEAK = Pattern.compile("\\b(undefined|NaN)\\b|\\[object Object]");

    private final TestContext ctx;

    public NegativeSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    // ------------------------------------------------------------ invalid route

    @When("I open the invalid route {string}")
    public void openInvalidRoute(String path) throws Exception {
        String url = Config.resolve(path);
        ctx.lastHttpResult = LinkChecker.get(url, UserAgents.DESKTOP); // status code (browsers hide it)
        new BasePage(ctx.driver()).open(url);                          // what the user actually sees
    }

    @Then("the page should show a friendly not-found message")
    public void friendlyNotFound() {
        String text = new BasePage(ctx.driver()).bodyText();
        assertThat(NOT_FOUND_TEXT.matcher(text).find())
                .as("page tells the user the page was not found. Page text: %s", abbreviate(text))
                .isTrue();
    }

    @Then("the page should not expose technical error details")
    public void noTechnicalLeak() {
        String text = new BasePage(ctx.driver()).bodyText();
        assertThat(TECHNICAL_LEAK.matcher(text).find())
                .as("no stack traces / raw errors shown to the user. Page text: %s", abbreviate(text))
                .isFalse();
    }

    // ------------------------------------------------------------ broken links

    @When("I collect all header links")
    public void collectHeaderLinks() {
        ctx.header().openMobileMenuIfPresent();
        ctx.collectedLinks = ctx.header().visibleLinks().stream()
                .map(l -> l.href().split("#")[0])
                .filter(h -> h.startsWith("http"))
                .filter(NegativeSteps::isCheckable)
                .distinct()
                .toList();
        ctx.log("Collected " + ctx.collectedLinks.size() + " header links");
    }

    @When("I collect all unique links on the page")
    public void collectAllLinks() {
        ctx.collectedLinks = new LinkedHashSet<>(ctx.home().allHttpLinks()).stream()
                .filter(NegativeSteps::isCheckable)
                .toList();
        ctx.log("Collected " + ctx.collectedLinks.size() + " links");
    }

    @When("I verify that every collected link responds")
    public void verifyLinks() {
        assertThat(ctx.collectedLinks).as("links collected").isNotEmpty();
        ctx.linkResults = LinkChecker.probeAll(ctx.collectedLinks, Config.getInt("linkcheck.threads", 8));
        List<LinkResult> restricted = ctx.linkResults.stream().filter(LinkResult::isRestricted).toList();
        if (!restricted.isEmpty()) {
            ctx.log("Warning - bot-restricted (not counted as broken): "
                    + restricted.stream().map(LinkResult::describe).toList());
        }
    }

    @Then("no collected link should be broken")
    public void noBrokenLinks() {
        List<String> broken = ctx.linkResults.stream()
                .filter(LinkResult::isBroken)
                .map(LinkResult::describe)
                .toList();
        assertThat(broken).as("broken links out of %d checked", ctx.linkResults.size()).isEmpty();
    }

    // ------------------------------------------------------------ hostile network (Chromium only)

    @Given("these network requests are blocked:")
    public void blockRequests(DataTable table) {
        List<String> patterns = table.asMaps().stream().map(m -> m.get("pattern")).toList();
        HasCdp cdp = (HasCdp) ctx.driver();
        cdp.executeCdpCommand("Network.enable", Map.<String, Object>of());
        cdp.executeCdpCommand("Network.setBlockedURLs", Map.<String, Object>of("urls", patterns));
    }

    @Given("the network is throttled to a slow connection")
    public void throttleNetwork() {
        HasCdp cdp = (HasCdp) ctx.driver();
        cdp.executeCdpCommand("Network.enable", Map.<String, Object>of());
        cdp.executeCdpCommand("Network.emulateNetworkConditions", Map.<String, Object>of(
                "offline", false,
                "latency", 500,
                "downloadThroughput", 100 * 1024,
                "uploadThroughput", 50 * 1024));
    }

    @Then("the hero heading should become visible within {int} seconds")
    public void heroWithin(int seconds) {
        assertThat(ctx.home().isHeroVisible(Duration.ofSeconds(seconds)))
                .as("hero heading visible within %ss on a slow network", seconds)
                .isTrue();
    }

    @Then("the page should not show an unhandled application error")
    public void noAppCrash() {
        String text = new BasePage(ctx.driver()).bodyText();
        assertThat(APP_CRASH.matcher(text).find())
                .as("no crash screen. Page text: %s", abbreviate(text))
                .isFalse();
    }

    @Then("the page should not render placeholder values such as undefined or NaN")
    public void noPlaceholderLeak() {
        String text = new BasePage(ctx.driver()).bodyText();
        assertThat(PLACEHOLDER_LEAK.matcher(text).find())
                .as("raw placeholder values leaked into the UI. Page text: %s", abbreviate(text))
                .isFalse();
    }

    // ------------------------------------------------------------ helpers

    private static boolean isCheckable(String url) {
        String host = URI.create(url).getHost();
        if (host == null) {
            return false;
        }
        return Config.skippedLinkDomains().stream().noneMatch(host::endsWith);
    }

    private static String abbreviate(String s) {
        String oneLine = s.replaceAll("\\s+", " ").trim();
        return oneLine.length() > 300 ? oneLine.substring(0, 300) + "..." : oneLine;
    }
}
