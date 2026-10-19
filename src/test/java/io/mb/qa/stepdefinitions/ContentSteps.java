package io.mb.qa.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.mb.qa.components.BannerComponent;
import io.mb.qa.components.BannerInfo;
import io.mb.qa.context.TestContext;
import io.mb.qa.pages.WhyMultiBankPage;
import io.mb.qa.utils.HttpResult;
import io.mb.qa.utils.LinkChecker;
import io.mb.qa.utils.UserAgents;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;

public class ContentSteps {

    private final TestContext ctx;
    private WhyMultiBankPage whyPage;

    public ContentSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    // ------------------------------------------------------------ banners

    @Then("the banner image {string} should be loaded inside the page body")
    public void bannerLoadedInBody(String srcFragment) {
        BannerComponent banners = new BannerComponent(ctx.driver());
        BannerInfo info = banners.inspect(srcFragment);
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(info.found()).as("banner '%s' is rendered and visible", srcFragment).isTrue();
        if (info.found()) {
            soft.assertThat(info.loaded()).as("banner '%s' actually loaded (not a broken image)", srcFragment).isTrue();
            soft.assertThat(info.insideBodyRegion()).as("banner '%s' sits between header and footer", srcFragment).isTrue();
            soft.assertThat(info.insideHeaderOrFooter()).as("banner '%s' is NOT inside header/footer", srcFragment).isFalse();
        }
        soft.assertAll();
    }

    // ------------------------------------------------------------ store links

    @Then("the {string} link should be visible")
    public void downloadLinkVisible(String label) {
        assertThat(ctx.home().isDownloadAppLinkVisible()).as("'%s' link visible", label).isTrue();
    }

    @When("I request the app download link as an {string} device")
    public void requestDownloadLink(String platform) throws Exception {
        String href = ctx.home().downloadAppHref();
        ctx.currentLink = href;
        ctx.lastHttpResult = LinkChecker.get(href, UserAgents.forPlatform(platform));
        ctx.log(platform + " device: " + href + " -> " + ctx.lastHttpResult.status()
                + " -> " + ctx.lastHttpResult.finalUrl());
    }

    @Then("the response should reference the {string} store")
    public void responseReferencesStore(String storeHost) {
        HttpResult r = ctx.lastHttpResult;
        boolean landedOnStore = r.finalHost() != null && r.finalHost().contains(storeHost);
        boolean bodyMentionsStore = r.body() != null && r.body().contains(storeHost);
        assertThat(landedOnStore || bodyMentionsStore)
                .as("link %s should lead to %s (final URL: %s)", ctx.currentLink, storeHost, r.finalUrl())
                .isTrue();
    }

    // ------------------------------------------------------------ About Us > Why MultiBank

    @When("I navigate to {string}")
    public void navigateTo(String linkText) {
        whyPage = new WhyMultiBankPage(ctx.driver());
        whyPage.navigateFromCurrentPage(linkText);
    }

    @Then("the page should show these sections:")
    public void pageShowsSections(DataTable table) {
        SoftAssertions soft = new SoftAssertions();
        for (Map<String, String> row : table.asMaps()) {
            String heading = row.get("heading");
            String text = row.getOrDefault("text", "");
            soft.assertThat(whyPage.hasHeading(heading)).as("heading '%s'", heading).isTrue();
            if (text != null && !text.isBlank()) {
                soft.assertThat(whyPage.containsText(text)).as("text '%s' under '%s'", text, heading).isTrue();
            }
        }
        soft.assertAll();
    }
}
