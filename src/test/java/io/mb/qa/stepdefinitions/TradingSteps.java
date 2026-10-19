package io.mb.qa.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.mb.qa.components.MarketList;
import io.mb.qa.components.MarketRow;
import io.mb.qa.config.Config;
import io.mb.qa.context.TestContext;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;

public class TradingSteps {

    private final TestContext ctx;

    public TradingSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    @Then("these market categories should list trading pairs")
    public void categoriesListPairs(DataTable table) {
        SoftAssertions soft = new SoftAssertions();
        for (Map<String, String> row : table.asMaps()) {
            String category = row.get("category");
            try {
                soft.assertThat(ctx.home().marketList(category).rows())
                        .as("trading pairs under '%s'", category).isNotEmpty();
            } catch (AssertionError e) {
                soft.fail(e.getMessage());
            }
        }
        soft.assertAll();
    }

    @Then("every entry in {string} should contain the fields:")
    public void homeEntriesContainFields(String category, DataTable table) {
        assertFields(ctx.home().marketList(category).rows(), table, category);
    }

    @Then("every entry in {string} should have a positive 24h change")
    public void gainersArePositive(String category) {
        assertDirection(ctx.home().marketList(category).rows(), category, true);
    }

    @Then("every entry in {string} should have a negative 24h change")
    public void losersAreNegative(String category) {
        assertDirection(ctx.home().marketList(category).rows(), category, false);
    }

    @Then("the spot market should list at least {int} trading pairs")
    public void spotMarketMinimum(int minimum) {
        int min = Math.max(minimum, 0);
        assertThat(ctx.explore().spotMarket().rows().size())
                .as("trading pairs in the spot market (configured floor: spot.min.pairs=%s)",
                        Config.get("spot.min.pairs", "10"))
                .isGreaterThanOrEqualTo(min);
    }

    @Then("every spot market entry should contain the fields:")
    public void spotEntriesContainFields(DataTable table) {
        MarketList list = ctx.explore().spotMarket();
        assertFields(list.rows(), table, "Spot market");
    }

    // ---------------------------------------------------------------- helpers


    private void assertFields(List<MarketRow> rows, DataTable table, String where) {
        List<String> fields = table.asMaps().stream().map(m -> m.get("field").toLowerCase()).toList();
        SoftAssertions soft = new SoftAssertions();
        for (MarketRow row : rows) {
            for (String field : fields) {
                switch (field) {
                    case "name" -> soft.assertThat(row.hasName())
                            .as("[%s] name in row: %s", where, row.oneLine()).isTrue();
                    case "price" -> soft.assertThat(row.hasPrice())
                            .as("[%s] price in row: %s", where, row.oneLine()).isTrue();
                    case "change" -> soft.assertThat(row.changePercent().isPresent())
                            .as("[%s] 24h change in row: %s", where, row.oneLine()).isTrue();
                    default -> throw new IllegalArgumentException("Unknown field '" + field
                            + "'. Supported: name, price, change");
                }
            }
        }
        soft.assertAll();
    }

    private void assertDirection(List<MarketRow> rows, String category, boolean positive) {
        SoftAssertions soft = new SoftAssertions();
        for (MarketRow row : rows) {
            double change = row.changePercent().orElse(Double.NaN);
            soft.assertThat(Double.isNaN(change)).as("[%s] has a %% change: %s", category, row.oneLine()).isFalse();
            if (!Double.isNaN(change)) {
                soft.assertThat(positive ? change > 0 : change < 0)
                        .as("[%s] expected %s change but got %s%% in: %s", category,
                                positive ? "positive" : "negative", change, row.oneLine())
                        .isTrue();
            }
        }
        soft.assertAll();
    }
}