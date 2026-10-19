# mb.io QA Automation - Selenium + Java + Cucumber

UI automation framework for **https://mb.io/en-AE** (Task 1) plus written QA strategy and release documents (Task 2).
Tests are written as plain-English Gherkin scenarios, so a non-programmer can read what is covered.

## Run it (one command)

Prerequisites: JDK 17+, Maven 3.8+, and Chrome (Firefox/Edge optional). Selenium Manager downloads the driver automatically.

```bash
mvn clean test
```

| Want to... | Command |
|---|---|
| Use another browser | `mvn clean test -Dbrowser=firefox` (chrome, firefox, edge) |
| Watch the browser | `mvn clean test -Dheadless=false` |
| Run only smoke tests | `mvn clean test -Dcucumber.filter.tags="@smoke"` |
| Run one area | `-Dcucumber.filter.tags="@navigation"` (also `@trading`, `@content`, `@negative`, `@network`) |
| Point at another URL | `mvn clean test -Dbase.url=https://mb.io/en` |

**Report:** open `target/cucumber-reports/cucumber.html` (JSON and JUnit XML are next to it). Failed scenarios include a screenshot and the URL at failure.

## How the code is organised

```
src/test/resources/features/     <- WHAT is tested (Gherkin, human readable)
src/test/java/io/mb/qa/
  stepdefinitions/               <- glue: one sentence in a feature = one Java method (no Selenium calls here beyond page objects)
  pages/                         <- Page Objects (HomePage, ExplorePage, WhyMultiBankPage)
  components/                    <- reusable UI parts (Header, MarketList, Banner) - ALL locators live in pages/components
  context/TestContext.java       <- per-scenario state shared between steps (PicoContainer injection)
  hooks/Hooks.java               <- browser start/stop, screenshot on failure, skip rules
  driver/DriverFactory.java      <- Chrome / Firefox / Edge creation
  utils/                         <- HTTP link checker, user agents
  config/Config.java             <- config.properties + -D overrides
```

Flow of one step: `feature file sentence -> step definition -> page object / component -> Selenium`.
Adding a test normally means adding rows to a Gherkin table (e.g. a new header item) - no Java.

## Scenario coverage (mapped to the brief)

| Brief requirement | Feature file | Notes |
|---|---|---|
| Navigation renders / links correct / viewports | `01_navigation.feature` | Outline over 4 desktop sizes; click-through verifies landing URL |
| Spot trading renders, grouping, data fields | `02_trading.feature` | Grouping is verified semantically: *gainers must be positive, losers negative* |
| Banners, store links, Why MultiBank | `03_content.feature` | Banners: loaded + between header and footer. Store links: requested as iOS and Android devices |
| Negative: invalid route, broken links, mobile viewport, loading timeout | `04_negative_and_edge_cases.feature` | **All four** implemented (two required) |
| Bonus: network responses, parameterisation, CI | `05_network_responses.feature`, Scenario Outlines, `.github/workflows/ui-tests.yml` | CI runs Chrome, Firefox and Edge as a matrix |

## Design decisions

- **Gherkin + step definitions + page objects**: readable specs, thin glue, locators isolated so a UI change touches one file.
- **Browser per scenario, no shared state**: scenarios are independent and can run in any order.
- **Content-shape assertions, not CSS classes**: market rows are validated by what they contain (a name, a price, a signed %), so a front-end refactor does not break tests.
- **HTTP checks beside browser checks**: status codes (404s, store redirects, broken links) are verified with a plain HTTP client - faster and more precise than the browser.
- **Soft assertions** list every failing row/field at once instead of stopping at the first.
- **Explicit waits only**, no `Thread.sleep`, no implicit waits.
- **Bot-protected domains** (LinkedIn, X, ...) are skipped; 401/403/429 are logged as warnings rather than counted as broken.
- **`@chromium-only`** scenarios use Chrome DevTools Protocol (request blocking, throttling) and are skipped, not failed, on Firefox.

## Assumptions and known limitations - please read

1. **Target site.** The brief names `trade.multibank.io`; I was asked to use `https://mb.io/en-AE`. The suite follows mb.io, and its structure differs from what the brief describes (e.g. spot markets appear as *Top Gainers / Trending Now / Top Losers* on the home page and a *Spot market* section on `/en/explore`).
2. **Selectors were not run against the live DOM.** I could read the served HTML of the home and explore pages, but the navigation and market lists are rendered client-side, so those locators are educated, resilient guesses (text/role/structure based). They are isolated in `components/HeaderComponent.java` and `components/MarketList.java`. First run may need small locator tweaks.
3. **Header items.** Only *Sign up* (-> `trade.mb.io/register`) and the logo are confirmed from the HTML. Add the real menu items to the tables in `01_navigation.feature`.
4. **Store links** go through a `mbio.go.link` smart link. The test accepts either a redirect to the store or a response that references the store; tighten it once the behaviour is confirmed.
5. **No sample execution report is committed.** This suite was written without being able to launch browsers or reach Maven Central from the authoring environment. The CI workflow produces one HTML report per browser as a downloadable artifact on every run - that is the cross-browser evidence. No accounts are created and no personal/financial data is submitted. The tests only read public pages and click the *Sign up* link without filling anything in.
 
## Task 2 and supporting documents


- [Task 2 - QA strategy answers](docs/TASK2_QA_STRATEGY.md)
- [Test plan](docs/TEST_PLAN.md)
- [Release readiness checklist](docs/RELEASE_READINESS_CHECKLIST.md)
- [Risk matrix](docs/RISK_MATRIX.md)
