package io.mb.qa.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.mb.qa.context.TestContext;
import io.mb.qa.pages.BasePage;
import java.time.Duration;
import org.openqa.selenium.Dimension;

/** Steps reused by many features: opening pages, resizing the browser, generic page checks. */
public class CommonSteps {

    private final TestContext ctx;

    public CommonSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    @Given("I open the home page")
    public void openHomePage() {
        ctx.home().open();
    }

    @Given("I open the explore page")
    public void openExplorePage() {
        ctx.explore().open();
    }

    @Given("I open the path {string}")
    public void openPath(String path) {
        new BasePage(ctx.driver()).open(path);
    }



    @Then("the hero heading should be visible")
    public void heroVisible() {
        assertThat(ctx.home().isHeroVisible(Duration.ofSeconds(20)))
                .as("hero heading visible")
                .isTrue();
    }
}
