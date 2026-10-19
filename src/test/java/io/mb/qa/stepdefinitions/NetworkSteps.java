package io.mb.qa.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.mb.qa.config.Config;
import io.mb.qa.context.TestContext;
import io.mb.qa.utils.LinkChecker;
import io.mb.qa.utils.UserAgents;

/** Plain-HTTP checks: fast, browser-independent, and they show the status code a browser hides. */
public class NetworkSteps {

    private final TestContext ctx;

    public NetworkSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    @When("I request {string} over HTTP")
    public void requestOverHttp(String path) throws Exception {
        ctx.lastHttpResult = LinkChecker.get(Config.resolve(path), UserAgents.DESKTOP);
    }

    @Then("the response status should be {int}")
    public void statusIs(int expected) {
        assertThat(ctx.lastHttpResult.status())
                .as("HTTP status of %s", ctx.lastHttpResult.requestedUrl())
                .isEqualTo(expected);
    }

    @Then("the response status should be below {int}")
    public void statusBelow(int limit) {
        assertThat(ctx.lastHttpResult.status())
                .as("HTTP status of %s", ctx.lastHttpResult.requestedUrl())
                .isLessThan(limit);
    }

    @Then("the response content type should contain {string}")
    public void contentTypeContains(String expected) {
        assertThat(ctx.lastHttpResult.contentType()).containsIgnoringCase(expected);
    }
}
