# Test report — swipe-ui-01

**Final verdict: FAIL.** AC-01 has an observed required-content mismatch. AC-02 and AC-03 passed in the tested build. AC-04 remains unverified because the available feed content did not scroll. The earlier blocked attempt is preserved below.

## Build and device

- Candidate per sanitized packet: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, branch `codex/ui-01-discovery`, base revision `049a764` (`dev`). APK identity/content was not inspected.
- Emulator: `emulator-5554`, Android API 35 AVD as identified in the packet. ADB reported device `sdk_gphone64_arm64`.
- Configured logical viewport: `wm size 390x844`; density override `160 dpi`. Emulator screenshot captures were PNGs at 1080 × 2340 physical pixels with this logical viewport override.
- Test date: 2026-11-14 local (Asia/Vladivostok; UTC 01:06).

## Earlier attempt (preserved)

At approximately 11:00 local, `/private/tmp/srastemsya-sdk.7DCrTs/platform-tools/adb devices` could not start the ADB daemon (`could not install *smartsocket* listener: Operation not permitted`). No criteria could be checked during that attempt. After the environment was made available, `adb devices -l` succeeded and listed `emulator-5554 device`; checks below were then performed.

## Acceptance results

| Criterion | Result | Observed evidence |
|---|---|---|
| AC-01 Initial visible state | **FAIL** | `s01_viewport.png` and `s01_home_again.png`: logo, bell, greeting, mood tile, blue swipe tile with caption/illustration/arrow, “Популярное рядом” heading, and selected Discovery tab are visible. Instead of the required horizontal cards, the section shows `UNAUTHENTICATED` and `Повторить`; no cards appear. Reference composition: `docs/specs/swipe-ui/screens/280-8319.png`. |
| AC-02 Tile hit area and legibility | **PASS (sampled)** | Tapping the blue tile's text area at logical coordinate (70,365) reached the next-step choice screen; tapping a separate left-interior blue background area at (45,365) also reached it. On `s01_home_again.png`, caption and arrow are visible and not covered by the illustration. This samples multiple non-decorative areas; it does not exhaustively probe every point. |
| AC-03 Immediate transition | **PASS** | Immediately after the (70,365) tap, `s01_tile_tap.png` showed “Выбираем вместе” and the device-choice options, establishing arrival at the next step. No independent screen-02 layout or behavior was assessed. |
| AC-04 Scroll and bottom navigation | **BLOCKED / NOT VERIFIED** | A vertical swipe in the Discovery feed area left the screen unchanged (`s01_scroll_check.png`); the feed had no cards to scroll because the popular section returned the authentication error. The bottom bar remained visible and Discovery selected, but an actual feed scroll could not be produced. This is not PASS. |

## Reproduction and evidence

- Select Discovery in the bottom navigation; observe `s01_home_again.png`.
- Tap the blue swipe tile at (70,365); observe immediate arrival in `s01_tile_tap.png`.
- Return to Discovery and tap the blue tile at (45,365); it again opens the next-step choice.
- Swipe vertically in the feed area; no feed movement occurs while popular cards are unavailable.
- Evidence PNGs are stored beside this report: `s01_viewport.png`, `s01_home_again.png`, `s01_tile_tap.png`, `s01_edge_tap2.png`, and `s01_scroll_check.png`.

The visual failure is the missing horizontal popular cards and displayed authentication error. No product changes were made. Results apply to the packet's stated candidate only; any candidate revision/build change requires affected checks to be rerun.

# Round 2 — current candidate

**Round-2 verdict: BLOCKED.** The app opened to its sign-in selection screen after reinstall, so Discovery was unavailable. Per the packet, I did not provision credentials, submit login information, clear app data, or alter authentication state. No round-2 criterion is marked PASS or FAIL based on unavailable screen-01 checks.

- **Candidate APK SHA-256:** `3b6c8f32bf044a360f3121e5395ba1ab6f9fdef186885af5872e817c268c9c4c` (matches the current sanitized packet).
- **Install method:** `adb -s emulator-5554 install -r /private/tmp/mobile-ui-01/androidApp/build/outputs/apk/debug/androidApp-debug.apk` → `Success`. App data was not cleared.
- **Device / viewport:** same `emulator-5554`; logical override remained 390 × 844 with density override 160 dpi. API 35 AVD per packet.
- **Observed state:** after launching `fefu.prime.srastem/.MainActivity`, `s01_r2_launch2.png` showed “Срастёмся” and “Выберите способ входа”, with “Номер телефона” and “Электронная почта” choices. Discovery and `/v1/home` were not reached in this round, so no backend response was observed.

| Criterion | Round-2 result | Reason |
|---|---|---|
| AC-01 Initial visible state | **BLOCKED** | Login selection screen prevents viewing Discovery / checking popular content. |
| AC-02 Tile hit area and legibility | **BLOCKED** | Discovery tile is not available to tap. |
| AC-03 Immediate transition | **BLOCKED** | Could not activate the Discovery tile. |
| AC-04 Scroll and bottom navigation | **BLOCKED** | Discovery feed is unavailable; no scroll attempt could be made. |

Round-2 evidence: `s01_r2_launch2.png` (login selection screen). Earlier round-1 evidence and results remain unchanged above. The current candidate hash was verified before the install; no implementation files were inspected or modified.
