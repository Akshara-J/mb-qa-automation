package io.mb.qa.pages;

import io.mb.qa.components.BannerComponent;
import io.mb.qa.components.MarketList;
import org.openqa.selenium.WebDriver;

public class ExplorePage extends BasePage {

    public static final String PATH = "/en/explore";

    public ExplorePage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        open(PATH);
    }

    public MarketList spotMarket() {
        return new MarketList(driver, "Spot market");
    }

    public BannerComponent banners() {
        return new BannerComponent(driver);
    }
}
