# FE-F01 test report

- Platform: Android only; iOS excluded by supplied scope.
- Revision/build: base revision `0092b0ba38d9a3813d558454154ce075ec71cd82` plus current uncommitted changes; latest APK SHA-256 `1136b5319305782219c4a03e0a50bac780ed3bae9987780d4fe471ae18d06ee2`.
- Current overall result: **PASS** for Android client scope after independent final adjudication below. Earlier partial observations remain documented; narrow EVENT staging membership is unknown and visual differences are non-blocking under the user's stated priority.

## Independent final adjudication (2026-11-13 14:16 UTC)

- Overall result: **PASS** for the scoped Android client requirements, applying the user's explicit priority of behavior and reasonable spec alignment over pixel-perfect Figma matching. The earlier PARTIAL verdict is superseded by this review.
- AC-01 / F01-01: **PASS**. Independent staging UI runs establish kind/category/date/budget/party-size selection, successful PLACE and broad EVENT result paths, and the narrow EVENT empty-state path. Developer-provided Android host-test evidence in the packet verifies EVENT query fields (`kind`, category, `localDate`, budget, party size), PLACE date omission, and omitted optional fields. This is client request mapping evidence, not a black-box network capture. Backend matching is out of scope; the empty narrow EVENT results do not negate verified client mapping.
- AC-02: **PASS**. Independent UI evidence covers negative and fractional budget and party size below 1 with budget, with field-associated blocking errors.
- AC-03: **PASS**. Independent UI evidence covers returning with values restored and changing a filter to submit a fresh request/results path.
- AC-04: **PASS**. Black-box evidence confirms heading/context and kind-dependent date visibility/interaction. Packet architecture evidence identifies UI Kit controls and theme tokens, and the feature documentation lists the corresponding public UI Kit dependencies. Visual sizing/spacing differences remain documented observations; under the user's stated priority they are not blockers.
- AC-05: **PASS**. The feature documentation describes categories, validation, candidate endpoint, filter flow, and FE-F02 boundary, consistent with the published feature spec and API contract.
- Evidence distinction: independent staging/UI observations are recorded in the linked prior reviews and screenshots. Build, host-test coverage, and architecture evidence are Developer-provided packet evidence. The shared-suite harness report exited before test execution and contributes no test results. No local backend was used or considered.

## Current acceptance mapping — Android client scope

| Criterion | Current result | Evidence and limits |
|---|---|---|
| AC-01 / F01-01 | **PASS** | Independent Android UI evidence covers single-device filter selection, valid PLACE and broad EVENT submissions, and the narrow EVENT empty state. Packet-provided Android host tests cover EVENT `kind`, category, `localDate`, budget and party-size serialization; PLACE date omission; and absent optional values. This establishes client query mapping. Narrow EVENT staging matching remains unknown because staging returned no candidates for those combinations; no backend interpretation is claimed. |
| AC-02 | **PASS** | Independent Android runs show negative and fractional budgets and party size 0 with a budget block submission with field-associated errors. |
| AC-03 | **PASS** | Independent Android evidence shows submitted filters restored on return, and a changed filter produces a fresh request/results path. |
| AC-04 | **PASS** | Independent UI evidence confirms heading/context, kind choice, EVENT date interaction and PLACE date suppression. Packet architecture evidence and feature documentation identify UI Kit controls and theme tokens. Figma sizing, spacing and button-width differences remain documented observations and are nonblocking under the stated behavior/spec-alignment priority. |
| AC-05 | **PASS** | Feature documentation covers filter behavior, server categories, validation, candidate endpoint and FE-F02 boundary, consistent with the public specification and API boundary. |

Current verdict: **PASS** for the scoped Android client. The local fixture check is excluded from this verdict. The owning shared suite harness exited before test execution and produced zero results; it contributes no test evidence. Direct Android host-test success is packet-provided evidence. iOS is outside this supplied scope.

## Superseded assessment history

The prior PARTIAL assessment and its observations are preserved below as historical evidence. They are superseded by the independent final adjudication, which accepts client query mapping based on packet host-test evidence and considers UI Kit/token architecture evidence alongside the black-box observations. The adjudication does not claim narrow EVENT staging matching.

## Latest evidence

- [Factual harness run](../../../tests/reports/20261113T140507Z-FE-F01-android-runtime.md) — package installation check passed; `run.sh` stdout was redirected to preserve its report because the current harness prints instead of writing the report file itself.
- [Latest independent runtime and visual review](../../../tests/reviews/20261113T140507Z-FE-F01-android.md)
- [Latest screenshots](../../../tests/evidence/FE-F01/20261113T140507Z-android/)

## Request composition follow-up (2026-11-13 14:08 UTC)

- [Independent request capture review](../../../tests/reviews/20261113T140820Z-FE-F01-request-capture.md)
- The prior Android evidence verifies client-side selection, submission, result/empty-state transitions, and filter restoration. A fresh narrow EVENT submission could not be correlated with usable emulator UI/logcat output in this follow-up. No request URL or query parameters were observed, and no Bearer token/session payload was collected.
- Consequently, serialization of category, `localDate`, budget, and `partySize`, and server-side filter matching remain unverified. This is an evidence limit, not a verified app failure; AC-01 remains PARTIAL.

## Prior evidence retained

- [Public API fixture run](../../../tests/reports/20261113T135319Z-FE-F01-public-api.md) — BLOCKED after health checks and challenge creation; fixture stopped accepting connections before authentication/catalog/feed verification.
- [Public API independent review](../../../tests/reviews/20261113T135319Z-FE-F01-public-api.md)
- [Earlier EVENT filter follow-up review](../../../tests/reviews/20261113T234300Z-FE-F01-android.md)
- [Earlier runtime report](../../../tests/reports/20261113T234300Z-FE-F01-android-runtime.md)
- [Earlier screenshots](../../../tests/evidence/FE-F01/20261113T234300Z-android/)
- [Prior PLACE validation/restore review](../../../tests/reviews/20261113T132933Z-FE-F01-android.md)
- [Prior Android runtime report](../../../tests/reports/20261113T133445Z-FE-F01-android-runtime.md)
- [Initial build-blocked report, preserved](../../../tests/reports/20261113T130005Z-FE-F01-android.md)

## Scope note

Acceptance uses the supplied API archive and `https://srastemsya.nixbi.ru`; local fixture checks are excluded. Narrow EVENT filters had no matching result in the available staging dataset, so their correctness cannot be concluded from this run.

## Independent final acceptance review (2026-11-13)

- Overall result: **PARTIAL**. See [independent final acceptance review](../../../tests/reviews/20261113T141345Z-FE-F01-final-acceptance.md).
- AC-01 is **PASS for client behavior/request composition** based on prior independent UI observations and the test packet's Android host-test coverage of EVENT/PLACE query serialization. The host tests are Developer-provided evidence, not a black-box network capture. Narrow EVENT staging membership remains unknown; backend matching is outside mobile implementation scope and is not inferred from empty results.
- AC-02, AC-03, and AC-05 remain **PASS** on referenced Android evidence and prior feature-documentation review.
- AC-04 remains **PARTIAL**: heading/context and kind/date behavior were observed; UI Kit component/token usage is not provable from screenshots. Figma comparison differences are recorded but are not by themselves treated as acceptance failures.
