# PM report — swipe-ui-01

## Decision

**BLOCKED; no PR.** The current candidate has no complete black-box PASS. Do not create a PR until all mandatory criteria are verified on the current candidate and Tester attests the validated revision.

## Acceptance and evidence

- Immutable AC-01…AC-04 are in `acceptance.md`, scoped to screen 01, its own actions and the immediate arrival needed for AC-03.
- Round 1 (`test_report.md`): AC-01 failed on the tested candidate because the “Популярное рядом” section rendered `UNAUTHENTICATED` / `Повторить` instead of horizontal cards. AC-02 sampled PASS; AC-03 PASS; AC-04 could not be verified without content to scroll. Evidence includes `s01_viewport.png`, interaction captures and scroll capture.
- Round 2 (`test_report.md`): verified candidate SHA `3b6c8f32bf044a360f3121e5395ba1ab6f9fdef186885af5872e817c268c9c4c`, installed without clearing app data. App opened at sign-in selection; screen 01 was unreachable, so AC-01…AC-04 were all BLOCKED for this revision. Evidence: `s01_r2_launch2.png`.
- Candidate includes Developer's fix for early home fetch before session restoration and additional scroll clearance. Build/host checks pass (47 tests), but that does not establish UI acceptance.

## Documentation review

Reviewed `home/FEATURE.md`, `feed/FEATURE.md`, `docs/design/home.md`, `docs/specs/swipe-ui/01-discovery.md`, shared guidance in `docs/specs/swipe-ui/README.md`, and `docs/android-testing-environment.md`. The feature/design docs describe the screen, API-fed dynamic popular collections, and tile route; no new/changed backend service or HTTP/event interface was reported, so no service-doc or contract update is required. Current Developer documentation is consistent with the spec and interface behavior.

## Blocker and safe auth review

`docs/contracts/backend-api-snapshot-2026-11-13.md` identifies `https://srastemsya.nixbi.ru/v1` as staging and requires a bearer token; catalog access requires a completed profile. `docs/backend-integration.md` documents synthetic contacts and OTP `0427` for the *dev* backend. `docs/contracts/discovery-v1.md` also restricts the mocked OTP behavior to dev, with rate limits. The previous FE-F03 test packet warns to make only one OTP verification attempt; prior staging evidence cited by Developer records an unsuccessful verification and later rate limiting. There is no public confirmation that the staging host currently enables the dev fixture, that cooldown is clear, or that the test profile has nonempty home collections. No authentication request was made and no credentials/tokens were sent to Tester or written to reports. Without a safe, documented profile-complete staging session and populated `/v1/home`, Tester cannot reach the required screen state; request a valid test session/setup before continuing.

## Reports

- Acceptance: `workflow/reports/swipe-ui-01/acceptance.md`
- Sanitized test packet: `workflow/reports/swipe-ui-01/test_packet.md`
- Developer: `workflow/reports/swipe-ui-01/dev_report.md`
- Tester: `workflow/reports/swipe-ui-01/test_report.md`
