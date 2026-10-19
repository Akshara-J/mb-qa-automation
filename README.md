# mb.io QA Automation - Selenium + Java + Cucumber

UI automation framework for **https://mb.io/en-AE** (Task 1) plus written QA strategy and release documents (Task 2).

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
src/test/resources/features/     <- WHAT is tested (Gherkin)
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

## Assumptions and known limitations 

1. **Target site.** The brief names `trade.multibank.io`; I was asked to use `https://mb.io/en-AE`. The suite follows mb.io, and its structure differs from what the brief describes (e.g. spot markets appear as *Top Gainers / Trending Now / Top Losers* on the home page and a *Spot market* section on `/en/explore`).
2. **Selectors were not run against the live DOM.** I could read the served HTML of the home and explore pages, but the navigation and market lists are rendered client-side, so those locators are educated, resilient guesses (text/role/structure based). They are isolated in `components/HeaderComponent.java` and `components/MarketList.java`. First run may need small locator tweaks.
3. **Header items.** Only *Sign up* (-> `trade.mb.io/register`) and the logo are confirmed from the HTML. Add the real menu items to the tables in `01_navigation.feature`.
4. **Store links** go through a `mbio.go.link` smart link. The test accepts either a redirect to the store or a response that references the store; tighten it once the behaviour is confirmed.
5. **No sample execution report is committed.** This suite was written without being able to launch browsers or reach Maven Central from the authoring environment. The CI workflow produces one HTML report per browser as a downloadable artifact on every run - that is the cross-browser evidence. No accounts are created and no personal/financial data is submitted. The tests only read public pages and click the *Sign up* link without filling anything in.
 
## Task 2 and supporting documents

# Task 2: QA Strategy

## 1. Where Would I Start?

The first two days would focus on understanding the application, its architecture, and its highest-risk areas rather than immediately starting testing.

I would begin by exploring the application from a customer’s perspective, covering sign-up, login, deposits, buying and selling assets, and withdrawals. I would identify every point where money, balances, or sensitive user data could change.

I would then meet with developers, product managers, support, and compliance teams to understand the critical business requirements, known issues, technical dependencies, and regulatory expectations. I would also review the existing builds, CI pipelines, crash reports, and available test coverage.

Based on these findings, I would create a prioritized risk assessment, define clear release-readiness criteria, and agree on the testing priorities with the team. Since the release timeline is only two weeks, testing would be driven by business impact and risk.

## 2. How Would I Test the Application?

My primary focus would be financial transactions and security, followed by other features according to their risk level.

Financial transactions: I would thoroughly test deposits, withdrawals, buy and sell orders, fees, rounding, balance updates, and transaction history. I would also test duplicate taps, repeated requests, network failures, and application crashes during transactions. Results displayed in the application would be validated against backend records and the financial ledger wherever accessible.

Security and compliance: I would verify authentication, session management, secure token storage, logout behavior, KYC verification, restricted-country access, and risk warnings. I would also check withdrawal limits and additional authentication requirements.

API testing: I would prioritize API automation because it allows faster and more reliable validation of critical business logic. Using Postman or an appropriate automation framework, I would test positive scenarios, negative scenarios, response codes, data integrity, authorization, and error handling.

Exploratory testing: I would use real devices to explore unexpected user behavior, including interrupting withdrawals, switching applications during transactions, and repeating actions after timeouts.

Device and network compatibility: I would test selected iOS and Android devices, including Samsung and lower-end devices, under normal, slow, unstable, and offline network conditions.

Automation testing: I would develop a small, stable UI smoke suite for the most important customer journeys while keeping the majority of business-logic validation at the API level.

Release-build testing: I would test the actual TestFlight and Google Play internal testing builds, including fresh installations, upgrades from the previous version, and compatibility with existing user data.

## 3. How Would I Integrate QA into a Sprint?

I would involve QA throughout the sprint rather than treating testing as the final phase.

During refinement: I would review requirements, clarify acceptance criteria, identify edge cases, and flag any story involving money, security, or sensitive data as high risk.

During sprint planning: I would estimate testing effort alongside development effort and ensure that high-risk functionality is implemented early enough to allow sufficient testing and defect resolution.

During development: I would prepare test scenarios while developers implement the features. Unit tests and API tests would run through CI pipelines, and I would collaborate with developers to review functionality before merging.

During testing: I would validate each story against its acceptance criteria on a test build and perform exploratory and regression testing to identify unintended side effects. Every defect would include reproducible steps, the build version, device and OS details, and relevant screenshots, videos, or logs.

Definition of Done: A story would be considered complete when its acceptance criteria are met, relevant automated tests are added, and no critical or high-severity defects remain open.

Before release: I would follow a go/no-go checklist, verify the release candidate, support a staged rollout, monitor production metrics, and participate in a retrospective to identify improvements.

## 4. What Would My Ideal Regression Suite Look Like?

I would organize the regression suite into multiple layers to balance speed, coverage, and reliability.

Unit tests: Developers would validate business rules such as fees, rounding, and input validation on every commit.

API regression tests: Automated tests would cover authentication, KYC, deposits, withdrawals, orders, and balance calculations. These would run on every relevant merge.

UI smoke tests: A 10–15-minute suite would cover login, market viewing, placing and cancelling an order, and basic deposit and withdrawal journeys. It would act as a release-build quality gate.

Nightly UI regression: A broader suite would validate critical customer journeys across selected devices, including failure scenarios such as insufficient funds, network loss, and interrupted transactions.

Manual pre-release testing: I would conduct a final exploratory pass on real devices using the release candidate.

Non-functional testing: I would include performance checks, security scanning, and basic accessibility validation.

The suite would use independent tests, explicit waits instead of arbitrary sleeps, meaningful tags, and clear failure evidence. Flaky tests would be investigated and fixed promptly, and every production defect would result in an appropriate regression test.

## 5. What Would Concern Me Most Before Launch?

My biggest concerns would be issues that could cause financial loss, compromise user accounts, or prevent the team from responding effectively to a production incident.

Financial integrity: Incorrect balances, duplicate orders, incorrect fees, or failed withdrawals that still debit an account could directly affect customers and create financial and regulatory consequences.

Account security: Account takeover, insecure session handling, and insufficient withdrawal protection could expose user funds.

Market-data and performance issues: Stale prices, slow order processing, or application instability during periods of high market activity could affect trading decisions and transactions.

Compliance failures: Incorrect KYC decisions, restricted-country access, or missing risk warnings could result in serious compliance issues.

Third-party dependencies: Payment and KYC providers might behave differently in production than in sandbox environments. I would therefore test timeouts, rejected requests, delayed responses, and recovery scenarios.

Operational readiness: A lack of monitoring, alerting, a tested kill switch, or rollback procedures could make an incident much harder to contain.

Release and compatibility risks: App-store rejection, upgrade failures, or corrupted local data could disrupt the launch or affect existing users.

To reduce these risks, I would recommend a controlled code freeze, additional review of financial transaction changes, and a staged rollout such as 1%, 10%, and then 100%, provided monitoring remains healthy at each stage.

If I could prioritize only three launch safeguards, they would be verified financial transaction flows, a tested kill switch with rollback capability, and real-time production monitoring with a designated on-call engineer.

# Test Plan

## Objective

Deliver a reliable first release within 14 days, minimizing the risk of losing, misrepresenting, or exposing user funds and sensitive data. The objective is to provide sufficient test evidence for an informed go/no-go decision.

## Scope

Testing would cover sign-up and login, KYC, deposits, withdrawals, market data, buy and sell orders, balances, notifications, settings, installation and upgrades, and the marketing website using the existing Selenium automation suite.

Features disabled for launch, administrative tools, and the internal implementation of third-party providers would be excluded from direct functional testing.

## Test Approach

Developers would be responsible for unit testing, while QA would focus on API automation, Appium-based mobile smoke and journey testing, Selenium/Cucumber web automation, exploratory testing on real devices, and basic performance, security, and accessibility checks.

A limited beta would be conducted using TestFlight and the Google Play internal testing track.

Testing priorities would be classified as follows:

- P0 – Critical: Financial transactions, balances, account security, and data integrity.
- P1 – High: Onboarding, KYC, market data, and other essential customer journeys.
- P2 – Normal: Remaining functionality, secondary settings, and lower-risk features.

## Test Environment

I would use a staging environment connected to sandbox payment and KYC services, with seeded accounts representing different states: new users, KYC-pending users, verified users, restricted users, and accounts with insufficient funds.

The device matrix would be selected using available usage analytics and would include representative iOS and Android devices. Network profiles would cover normal Wi-Fi, slow connections, intermittent connectivity, and complete network loss.

## Release Entry and Exit Criteria

Testing would begin once a stable build, working test environment, test accounts, and agreed acceptance criteria were available.

The release would be considered ready when all P0 tests pass, no critical or high-severity defects remain open, API and smoke suites pass on the exact release candidate, required security checks are completed, and monitoring, rollback, and operational support are ready.

Any unresolved risk affecting financial integrity, security, or compliance would require explicit review and approval before release.

## Two-Week Execution Plan

Days 1–2: Discovery and preparation

Understand the application, review requirements and existing automation, identify high-risk areas, prepare test accounts and environments, and establish release criteria.

Days 3–5: Critical API and financial testing

Automate core money flows, validate backend balances and ledger records, build the initial smoke suite, and conduct exploratory testing on deposits, withdrawals, and orders.

Days 6–8: Negative scenarios and device coverage

Test transaction interruptions, duplicate requests, network failures, security controls, device compatibility, and baseline performance.

Days 9–10: Fix verification and release-candidate testing

Retest resolved defects, execute regression tests, validate store builds, and test fresh installations and upgrades.

Day 11: Code freeze and full regression

Freeze feature changes, execute the planned regression suite, review outstanding defects, and confirm the release candidate's stability.

Days 12–13: Operational and store readiness

Verify monitoring, alerts, rollback procedures, kill-switch behavior, store-build smoke tests, and launch support arrangements.

Day 14: Go/no-go and staged rollout

Review the final test evidence, obtain stakeholder sign-offs, begin the staged release if approved, and monitor production dashboards closely.

## Defect Management

Defects would be classified according to business impact.

- Critical: Financial loss, security compromise, data loss, or a launch-blocking crash.
- High: A core customer journey is broken with no reasonable workaround.
- Medium: Significant functionality is affected, but a workaround exists.
- Low: Minor UI, usability, or cosmetic issues.

Defect triage would occur daily, with critical and high-severity defects blocking release until resolved and verified or formally reassessed under the agreed release policy.

## Web Automation

The existing Selenium/Cucumber suite would execute against Chrome, Firefox, and Edge in CI. Each browser run would produce a separate test report, making cross-browser failures easier to identify and investigate. Implementation details and known limitations would be documented in the README.

# Release Checklist

Each item would be marked Pass, Fail, or N/A, with a named owner and supporting evidence. Any failed blocker would result in a no-go decision until resolved or formally reassessed by the authorized stakeholders.

## 1. Financial Integrity and Security — Release Blockers

- Deposit, buy, sell, and withdrawal journeys work end to end, with results matching the backend ledger.
- Orders are protected against duplicate submissions, retries, and application termination.
- Fees, rounding, balances, and currency formatting are correct.
- Login, session handling, and logout work correctly, and tokens are stored securely.
- Withdrawal protections, including re-authentication, transaction limits, and new-address checks, are verified where applicable.
- KYC, restricted-country controls, and risk warnings behave as expected.
- The security review is complete, with no unresolved critical or high-severity findings.
- No secrets, test credentials, or inappropriate debug settings are present in the release build.

## 2. Functional Quality

- All P0 and P1 tests pass on the release candidate.
- No critical or high-severity defects remain open.
- API and smoke suites pass on the exact build intended for release.
- Testing covers the agreed device matrix, including a lower-end device.
- Slow-network, offline, and transaction-interruption scenarios are verified.
- Fresh installation and upgrade from the previous version are tested.
- Market-data screens meet agreed performance expectations.
- Basic accessibility checks are completed.

## 3. Compliance and App-Store Readiness

- Privacy policy and terms of service are accessible.
- Store listings, age ratings, and data disclosures are complete.
- Store submissions are made early enough to allow time for review or rejection.
- Legal and compliance approvals are recorded.

## 4. Operational Readiness

- Crash reporting and analytics work in the release build.
- Alerts cover errors and failed orders, deposits, and withdrawals.
- An on-call engineer is assigned for launch week.
- The kill switch, rollback procedure, and force-update mechanism are tested where supported.
- The staged rollout plan includes clear monitoring thresholds and stop criteria.
- The customer support team is briefed on the release and escalation process.

## 5. Final Sign-Off

QA, Engineering, Product, and Security/Compliance representatives would record their names, decisions, and dates before the release proceeds.

# Risk Assessment

I would score each risk using Likelihood × Impact, with both values ranging from 1 to 5.

Scores of 15–25 would be treated as critical, 8–14 as high, and below 8 as moderate. These initial scores are estimates and would be reviewed after discovery and technical discussions.

## Critical Risks

1\. Incorrect balances, duplicate orders, or incorrect fees — Score: 15

Likelihood: 3. Impact: 5.

I would validate financial transactions against backend ledger records, test fee and rounding calculations, and verify idempotency to prevent duplicate transactions.

2\. Account takeover — Score: 15

Likelihood: 3. Impact: 5.

I would prioritize security reviews, authentication testing, session controls, and additional verification for sensitive withdrawal operations.

3\. Stale market prices during volatility — Score: 15

Likelihood: 3. Impact: 5.

I would test market-data freshness, API latency, and application performance under increased load.

4\. Lack of monitoring, kill switch, or rollback — Score: 15

Likelihood: 3. Impact: 5.

I would verify production alerts, feature flags, emergency shutdown procedures, and staged rollout controls before launch.

5\. Regression failures without sufficient automated coverage — Score: 16

Likelihood: 4. Impact: 4.

I would implement CI-based API and smoke tests, strengthen regression coverage for critical journeys, and enforce a controlled code freeze.

## High Risks

6\. KYC or regulatory failures — Score: 10

Likelihood: 2. Impact: 5.

I would work with compliance stakeholders to validate KYC states, restricted-country controls, and relevant regulatory requirements.

7\. Third-party providers behaving differently in production — Score: 12

Likelihood: 3. Impact: 4.

I would test timeout handling, rejected requests, delayed callbacks, and recovery behavior, while documenting the limitations of sandbox testing.

8\. Device and operating-system fragmentation — Score: 12

Likelihood: 4. Impact: 3.

I would use analytics to select representative devices and prioritize critical journeys across supported OS versions and screen sizes.

9\. Network loss during financial transactions — Score: 12

Likelihood: 3. Impact: 4.

I would simulate network interruptions during deposits, withdrawals, and order submission, then verify transaction reconciliation and duplicate prevention.

## Moderate Risks

10\. App-store rejection — Score: 8

Likelihood: 2. Impact: 4.

I would submit the application early, validate store requirements, and allow time for potential corrections.

11\. Upgrade-related data corruption — Score: 8

Likelihood: 2. Impact: 4.

I would test upgrades from the previous production version and verify that existing user data and application state remain intact.

12\. Broken website content or links — Score: 6

Likelihood: 3. Impact: 2.

I would run the existing Selenium regression suite across Chrome, Firefox, and Edge to identify broken navigation, content, and links.

## Final Recommendation

My release decision would be based on evidence rather than the deadline alone. The application should launch only when critical financial flows are verified, security and compliance checks are complete, release-blocking defects are resolved, and the team can monitor and control the rollout.

The goal is not simply to complete testing within 14 days; it is to make the release as safe and reliable as possible within that timeframe.
