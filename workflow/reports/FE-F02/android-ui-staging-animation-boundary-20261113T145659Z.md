# FE-F02 staging, animation, and boundary follow-up — 2026-11-13 14:56 UTC

- Platform: Android API 35 emulator `Srastemsya35`; iOS excluded.
- Worktree revision: base `a67098fea02e445bdb3f64d85407fcf9a3b3933d`, uncommitted state.
- APK: `/private/tmp/mobile-FE-F02/androidApp/build/outputs/apk/debug/androidApp-debug.apk`; built 2026-11-14 00:49:28 local; SHA-256 `4d712fcdc88911a2a3ffc74afe54891388738d455f70f5144722ce1b9a9f7678`. This build predates product-owner AC-09 split-pool instruction.

## Staging/API attempt

The staging hostname was reachable (root returned expected nginx 404). The public mobile API schema in the supplied archive documents `POST /v1/auth/challenges` with `{channel, destination}` and `POST /v1/auth/challenges/{id}/verify` with a four-digit string code. One challenge request for the provided test fixture returned 201; verification with the provided code returned 422. A subsequent challenge request returned 429. The successful challenge ID was not retained, so I could not verify against it. Per PM direction, no further challenge was requested after 429. No API access token was obtained; consequently no authenticated feed/candidate/detail query ran. No credential or session data is recorded here.

The app currently opens directly to a generic Profile (“Участник 1”) and Feed flow without a visible login prompt. Candidate content observed through this app build cannot be attributed to staging. Staging-backed metadata is therefore BLOCKED, not verified.

## UI behavior observed on this APK

- Entered Feed → single-device → filters → Show collection. Card hierarchy reported title/category/price/address; the portrait screenshot showed cover illustration and title but the metadata row rendered as blank pill/skeleton shapes. No description text was visible in the screenshot. The app UI/API source was not inspected.
- Directional animation: an intermediate ADB screen capture during a left swipe showed the active card translating and tilting left while decorative cards remained behind it; the next card then promoted to position 2/20. Qualitative left-exit/stack promotion observed. No right swipe animation capture was made in this run.
- Pool boundary: this APK exposed `1 / 20` through `20 / 20`. Skipping the last card led directly to “Больше вариантов нет”; the settled hierarchy contained that status and “Изменить фильтры,” with no loading message and no next local pool. The intermediate capture during the last swipe showed the card animating offscreen. Thus AC-04 loading suppression is not verified, and this pre-AC-09 build does not meet AC-09's target of 10 per visible pool. This is not evidence that the staging API returns 20 or changed page size; this UI data source was not authenticated/attributed.
- Landscape on this build: screenshot showed an oversized card image cropped across most of the screen; card text was absent from the hierarchy. Scrolling exposed vote controls and filter button, but not the card text. Rotation returned to portrait afterward. AC-06 preservation was previously verified on a preceding rebuilt APK; this landscape visual result is specific to this APK hash.

## Result / command evidence

- Commands/actions included `curl -I https://srastemsya.nixbi.ru`, the documented staging auth sequence (one challenge only, then verify; no subsequent challenge after 429), `adb install -r`, visible UI taps/swipes, `uiautomator dump`, and `adb exec-out screencap -p`. Root endpoint returned 404; auth statuses were 201, 422, then 429; install/UI capture succeeded.
- AC-02 directional left animation: PASS for qualitative left exit/stack promotion; right animation not rechecked.
- AC-04: BLOCKED/incomplete for loading state; observed exhausted pool, no next-page state available.
- AC-05 staging details: BLOCKED for API-backed field completeness. Visible card screenshot had title and cover illustration; metadata row appeared as blank placeholder pills although accessibility text listed category, price status, and city; no description visible.
- AC-09: NOT ACCEPTED on this pre-clarification build: visible pool size was 20, not 10. Retest required on post-AC-09 build, including local split continuity/order.
- Overall for requested remaining checks: BLOCKED/incomplete by staging auth throttle and absence of next-page response; AC-09 requires the pending new build.
- Captures: `/private/tmp/fe-f02-portrait-latest-settled.png`, `/private/tmp/fe-f02-swipe-midpoint.png`, `/private/tmp/fe-f02-boundary-mid.png`, `/private/tmp/fe-f02-exhausted.png`, `/private/tmp/fe-f02-landscape-latest.png`.
