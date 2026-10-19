# Release Readiness Checklist

Mark each item Pass / Fail / N/A with an owner and evidence link. **Any Fail in a "Blocker" section means no-go.**

## Blockers - money and security
- [ ] Deposit, buy, sell, withdraw verified end to end; balances match the backend ledger
- [ ] Order submission is idempotent (double tap, retry after timeout, app killed mid-order)
- [ ] Fees, rounding and currency formatting verified against specification
- [ ] Authentication, session expiry and logout correct; tokens stored in secure storage
- [ ] Withdrawal protections in place (re-auth/confirmation, limits, new-address safeguards)
- [ ] KYC and restricted-country rules enforced; required risk warnings displayed
- [ ] Penetration/security review done; no open critical or high findings
- [ ] No secrets, test endpoints or debug flags in the release build

## Quality gates
- [ ] All P0 and P1 test cases executed and passed on the release candidate
- [ ] No open Critical or High defects; Medium defects reviewed and accepted
- [ ] API regression and UI smoke suites green on the exact build being shipped
- [ ] Tested on the agreed device/OS matrix, including a low-end device
- [ ] Offline, slow network, interruption and background/foreground behaviour verified
- [ ] Fresh install and upgrade-from-previous-version tested
- [ ] Performance acceptable on market-data screens; no memory or battery regressions
- [ ] Accessibility basics (font scaling, contrast, screen reader on key flows)

## Compliance and store
- [ ] Privacy policy, terms and consent flows present and linked
- [ ] App Store and Google Play metadata, screenshots, age rating and data-safety forms complete
- [ ] Store review submitted early enough to absorb one rejection cycle
- [ ] Legal/compliance sign-off recorded

## Operations and recovery
- [ ] Crash reporting and analytics live and verified in the release build
- [ ] Dashboards and alerts for errors, order failures, deposit/withdrawal failures and latency
- [ ] On-call owner and escalation path named for launch week
- [ ] Remote kill switch / feature flags tested for risky features
- [ ] Rollback or hotfix procedure rehearsed; minimum-supported-version (force update) mechanism works
- [ ] Staged rollout plan with explicit stop criteria (e.g. crash-free rate, failed-order rate)
- [ ] Customer support briefed with known issues and FAQs

## Sign-off
| Role | Name | Decision | Date |
|---|---|---|---|
| QA | | | |
| Engineering | | | |
| Product | | | |
| Security/Compliance | | | |
