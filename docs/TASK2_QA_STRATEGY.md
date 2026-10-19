# Task 2 - QA Strategy & Thinking

Context: iOS + Android trading app, two weeks to first public release, no tests, no QA docs, real user funds.

## 1. Where do I start?

Day 1-2 is about **information, not testing**.

- **Understand the product and the money path.** Walk the app as a user: onboarding, deposit, buy/sell, withdraw. Draw the flow of funds and mark every place where money or balances change state.
- **Talk to people.** Developers (what is fragile, what shipped last-minute), product (what must not fail at launch), support/compliance (what regulators require), and whoever owns the backend and payment providers.
- **Inventory what exists:** builds and environments, CI, crash reporting, analytics, feature flags, API docs, existing bugs.
- **Agree a definition of "launch-ready"** with the team: explicit go/no-go criteria, written down.
- **Build a risk list** (see the risk matrix) and rank by *impact on user funds x likelihood*. Everything after this is prioritised by that list, because two weeks cannot cover everything.

## 2. How would I approach testing this app?

Risk-based, layered, and honest about what two weeks allows.

- **Money first.** Deposits, withdrawals, order placement, balances, fees, rounding, and idempotency (double tap, retry after timeout, app killed mid-order). Verify against the backend ledger, not only the UI.
- **Security and compliance.** Auth, session expiry, biometrics/PIN, token storage on device, certificate pinning, jailbreak/root behaviour, KYC gating, geo-restrictions, risk warnings shown where required.
- **API layer.** Automated contract and functional tests (auth, orders, balances, error codes). Cheapest and most stable place to find serious bugs.
- **Exploratory sessions** with charters (e.g. "interrupt every step of a withdrawal") on real devices - this finds what scripts do not.
- **Device/OS matrix** chosen from analytics or market data: a handful of iOS versions/devices and Android OEMs (Samsung, Pixel, Xiaomi) at small/large screens, slow networks, offline, background/foreground, interruptions (calls, notifications).
- **Non-functional:** performance under market-data load, battery/memory, accessibility basics, localisation and number/currency formats.
- **Automation strategy:** automate the stable, high-value paths (API first, then a thin UI layer with Appium/Detox-style tooling); keep the UI suite small so it stays reliable.
- **Release plumbing:** test the actual store builds (TestFlight / internal track), upgrade paths, and permissions prompts.

## 3. What does QA look like inside a sprint?

1. **Ticket creation / refinement:** QA joins refinement. Every story gets testable acceptance criteria (Given/When/Then), edge cases, and a note on data/environments needed. Money-touching stories are flagged high-risk. "Three amigos" for anything non-trivial.
2. **Sprint planning:** testing effort is estimated alongside dev; risky stories are scheduled early, not on the last day.
3. **During development:** QA writes test cases and automation from the criteria in parallel; developers write unit tests; PRs run lint, unit and API tests in CI. Developers demo to QA before merge for fast feedback ("shift left").
4. **Testing on a build:** every merged story is deployed to a test environment (feature flags where useful), verified against its criteria, then explored for side effects. Bugs are raised with steps, build, device, logs/video and severity.
5. **Definition of Done:** acceptance criteria met, automated tests added, no open critical/high bugs, analytics and error handling verified, docs updated.
6. **Regression:** the automated regression suite runs on every merge/nightly; before release a focused manual smoke on real devices covers what automation cannot.
7. **Release and after:** go/no-go against the checklist, staged rollout, monitoring dashboards watched, and a short retro on escaped defects feeding back into the suite.

## 4. What does my ideal regression suite look like?

A pyramid, tiered by speed and risk:

- **Unit tests (developers):** fast, run on every commit - calculations, fee/rounding logic, validation.
- **API regression (largest QA suite):** auth, KYC state transitions, deposit/withdraw, order lifecycle, balance reconciliation, error and rate-limit behaviour. Runs on every merge, minutes not hours.
- **Smoke suite (UI, ~10-15 min):** install/login, view market, place and cancel a small order in a sandbox, deposit/withdraw happy path. Gates every build.
- **Core UI regression (nightly):** the critical user journeys on a small device matrix (cloud device farm), plus negative cases (insufficient funds, expired session, lost network).
- **Manual/exploratory pre-release pass:** real devices, real payment sandbox, upgrade-from-previous-version, interruptions.
- **Non-functional checks:** performance baseline on market data screens, security scan (SAST/dependency), accessibility smoke.
- **Properties of a good suite:** deterministic (no sleeps, controlled test data), independent, tagged (`smoke`, `regression`, `money`), reported with evidence (screenshots/video/logs), flaky tests quarantined and fixed quickly, and every escaped production bug adds a test.

## 5. What would keep me up at night?

1. **Money bugs:** a wrong balance, duplicated order, failed withdrawal that still debits, rounding or fee errors. Irreversible and trust-destroying.
2. **Account takeover and security:** weak session handling, tokens stored insecurely, phishing/SIM-swap exposure, no withdrawal protections (device confirmation, whitelist addresses, cooldowns).
3. **Market-data and order integrity under volatility:** stale prices, latency, orders at a price the user did not see, behaviour when the backend is slow during a crash or spike - exactly when users are most active.
4. **Regulatory and compliance gaps:** KYC/AML flow errors, restricted-country access, missing risk warnings, privacy and data handling. Launching in the wrong place is a legal, not just a quality, problem.
5. **Third-party dependencies:** payment, KYC and market-data providers failing or behaving differently in production than in sandbox.
6. **No safety net after release:** no crash reporting, no monitoring or alerts, no remote kill-switch/feature flags, no tested rollback - a public launch with no way to react quickly.
7. **Store-review and upgrade surprises:** rejection days before launch, or an update that corrupts existing local data.
8. **Fast shipping, no tests:** unknown regressions on every change in the final two weeks. Mitigation is a code freeze, strict change control on money paths, and a staged rollout (e.g. 1% -> 10% -> 100%) with clear rollback criteria.

If I could only secure three things before launch: **verified money flows end to end, a staged rollout with a kill switch, and live monitoring with someone on call.**
