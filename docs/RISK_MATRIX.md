# Risk Matrix

Likelihood (L) and Impact (I) scored 1 (low) to 5 (high); **Score = L x I**. 15+ = critical, 8-14 = high, below 8 = moderate. Scores are initial estimates to be revised after the day 1-2 discovery.

| # | Risk | L | I | Score | Mitigation | Owner |
|---|---|---|---|---|---|---|
| 1 | Incorrect balance, duplicate or lost order, wrong fees/rounding | 3 | 5 | 15 | API tests against ledger, idempotency tests, change control on money paths | QA + Backend |
| 2 | Account takeover (weak session/token storage, no withdrawal safeguards) | 3 | 5 | 15 | Security review, secure storage checks, withdrawal re-auth and limits | Security + Mobile |
| 3 | Stale or wrong market data / order at unseen price during volatility | 3 | 5 | 15 | Load and latency tests, staleness indicators, price-slippage rules verified | QA + Backend |
| 4 | Regulatory/KYC gaps, restricted-region access, missing risk warnings | 2 | 5 | 10 | Compliance review, KYC state-machine tests, geo-restriction tests | Compliance + QA |
| 5 | Third-party provider failure (payments, KYC, data) differing from sandbox | 3 | 4 | 12 | Contract tests, failure-injection, timeouts and fallbacks, provider status monitoring | Backend + QA |
| 6 | No monitoring, kill switch or tested rollback at launch | 3 | 5 | 15 | Crash reporting, alerts, feature flags, rehearsed hotfix, staged rollout | Engineering |
| 7 | Regression from rapid shipping with no automated safety net | 4 | 4 | 16 | Smoke + API regression in CI, code freeze, release candidate gating | QA + Dev |
| 8 | Device/OS fragmentation defects (esp. Android OEMs, low-end devices) | 4 | 3 | 12 | Analytics-based device matrix, cloud device farm | QA |
| 9 | App store rejection or review delay | 2 | 4 | 8 | Early submission, store-guideline review, metadata checklist | Product + Mobile |
| 10 | Upgrade path corrupts local data or sessions | 2 | 4 | 8 | Upgrade tests from previous builds, migration tests | QA + Mobile |
| 11 | Network interruptions mid-transaction leave inconsistent state | 3 | 4 | 12 | Offline/flaky-network tests, server-side reconciliation, clear user messaging | QA + Backend |
| 12 | Marketing/web content or links broken at launch | 3 | 2 | 6 | Automated Selenium suite (this repo): link checks, banners, store links | QA |

## Heat map (L across, I down)
| I \ L | 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|
| **5** | | #4 | #1 #2 #3 #6 | | |
| **4** | | #9 #10 | #5 #11 | #7 | |
| **3** | | | | #8 | |
| **2** | | | #12 | | |
| **1** | | | | | |
