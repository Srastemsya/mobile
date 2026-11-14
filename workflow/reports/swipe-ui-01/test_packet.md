# Tester packet — swipe-ui-01

Acceptance criteria are fixed in `acceptance.md`; test only AC-01 through AC-04. Scope is the Discovery screen's visible state, actions originating there, vertical feed scrolling and fixed/selected bottom tab bar, plus the immediate transition landing needed to establish screen 02. Do not assess screen 02's own layout or behavior, or any other screen. Reference: 390 × 844 px with platform status/safe insets treated separately; PNG `docs/specs/swipe-ui/screens/280-8319.png`.

## Current candidate

- Worktree: `/private/tmp/mobile-ui-01`, branch `codex/ui-01-discovery`, based on `dev` at `049a764`.
- Latest candidate: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `3b6c8f32bf044a360f3121e5395ba1ab6f9fdef186885af5872e817c268c9c4c`.
- Developer reports Android assemble + shared host tests passed (47 tests, 0 failures/errors/skips) for this candidate.
- Available device: documented API 35 AVD `Srastemsya35`, registered as `emulator-5554`.

## Data-dependent visible state

A prior black-box attempt on the earlier candidate showed `UNAUTHENTICATED` and `Повторить` instead of the horizontal “Популярное рядом” cards, so AC-01 failed; AC-04 could not be exercised without scrollable content. The latest candidate removes an early home request before registration/session restoration and adds scroll clearance. Developer reports `/v1/home` requires an authenticated, profile-complete session and nonempty collections. Do not attempt to provision credentials or alter auth state. Recheck using the available app state after installing the candidate without clearing app data. If the API still returns the auth/error state or there is no scrollable content, record AC-01/AC-04 as BLOCKED with factual evidence and the exact message; do not count as PASS. AC-02/AC-03 should be rechecked on the latest candidate.

## Public context and reporting

Allowed supplementary docs: `docs/specs/swipe-ui/01-discovery.md`, `docs/specs/swipe-ui/README.md` (shared visual-check guidance), `shared/src/commonMain/kotlin/fefu/prime/srastem/home/FEATURE.md`, `docs/design/home.md`, and `docs/android-testing-environment.md` if needed. Record actual candidate identity/build, device/viewport, exact steps and observations, evidence paths/timestamps, and unavailable checks in Tester-owned `workflow/reports/swipe-ui-01/test_report.md`; preserve the first attempt. Do not inspect source, diffs, test implementation, other screen specs, or Developer reports/messages. Do not alter Developer-owned files.
