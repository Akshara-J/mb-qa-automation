# Test Plan - Mobile Trading App (iOS + Android) and Web Automation

## 1. Objectives
Release a first public version with **no defect that can lose, misstate or expose user funds or data**, and with enough evidence for an informed go/no-go decision in 14 days.

## 2. Scope
**In scope:** registration/login/KYC, deposits, withdrawals, market data, order placement/cancellation, portfolio/balances, notifications, settings/security, app install/upgrade, public marketing site (covered by the Selenium suite in this repo).
**Out of scope (this release):** features behind flags not shipping at launch, back-office/admin tools, third-party providers' own internals (we test integration points, not their systems).

## 3. Test approach
| Level | Who | What | Tooling (proposed) |
|---|---|---|---|
| Unit | Developers | Fee, rounding, validation logic | Platform unit-test frameworks |
| API | QA + Dev | Auth, orders, balances, error codes, idempotency | REST client + automated collection/suite |
| UI automation | QA | Smoke + critical journeys | Appium (mobile), Selenium/Cucumber (web, this repo) |
| Exploratory | QA | Charter-based sessions on real devices | Session notes |
| Non-functional | QA + Dev | Performance, security scan, accessibility smoke | Profilers, SAST, dependency scan |
| UAT / beta | Product + testers | Real-world usage with limited users | TestFlight / Play internal track |

Risk-based prioritisation: **P0** money and security, **P1** onboarding and market data, **P2** everything else.

## 4. Environments and data
- Test and staging backends with sandbox payment/KYC providers; production-like config on staging.
- Seeded accounts for each state: new, KYC-pending, verified, restricted, insufficient funds.
- Device matrix from analytics: iOS (current and two prior majors), Android (current plus popular OEMs and a low-end device), small and large screens.
- Network profiles: Wi-Fi, 4G, slow 3G, offline, flaky.

## 5. Entry and exit criteria
**Entry:** build installable from a store channel, test accounts and sandbox access ready, acceptance criteria available.
**Exit (go):** all P0 tests pass; no open critical/high defects; smoke and API regression green on the release candidate; security checks done; monitoring and rollback ready (see checklist).

## 6. Two-week schedule
| Days | Focus |
|---|---|
| 1-2 | Discovery, risk list, environments, test accounts, smoke checklist |
| 3-5 | API tests for money paths, first automated smoke, exploratory on deposits/withdrawals/orders |
| 6-8 | Device matrix, negative and interruption testing, security review, performance baseline |
| 9-10 | Fix verification, regression on release candidate, store-build and upgrade testing |
| 11 | Code freeze, full regression, go/no-go prep |
| 12-13 | Beta/staged-rollout preparation, monitoring checks, final smoke on store builds |
| 14 | Go/no-go, staged release, watch dashboards |

## 7. Defect management
Severity: **Critical** (funds/security/data loss, crash on launch), **High** (core flow broken, no workaround), **Medium**, **Low**. Triage daily; critical/high block release.

## 8. Deliverables
Test plan (this), risk matrix, release readiness checklist, automated suites, test execution reports, defect log, release go/no-go summary.

## 9. Web automation (this repository)
Cucumber features under `src/test/resources/features` run on Chrome, Firefox and Edge in CI; reports are uploaded per browser. See the README for assumptions and limitations.
