# FE-F03 independent test report

Tested 2026-11-13 15:53 UTC. Scope: Android single-device mode. Source revision reported by `git rev-parse HEAD`: `50af9d72c70dc827ad798056e3e516799362aee5`. Candidate working tree was uncommitted per packet; APK SHA-256: `3da6c2f880fb3eaf744706af9d8ca4088e9dbbbabd5e868e2e5a762f14f8f218`.

The prescribed build/host-test command (`:androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain`) completed successfully. APK installation on AVD `Srastemsya35` completed successfully. The external API returned `401 UNAUTHENTICATED` to a read-only unauthenticated feed request; the one permitted test-account OTP verification returned `200` with a complete profile. No authentication material is recorded here.

## Acceptance results

| Criterion | Result | Evidence |
|---|---|---|
| AC-01 — first LIKE result and catalog details | BLOCKED | Could not open or inspect the installed app UI. No product behavior was observed. |
| AC-02 — automatic paging, local pools of 10, loading state | BLOCKED | Could not operate the app or observe candidate-page requests. Pool size 10 was not verified. |
| AC-03 — exhausted feed and retained filters | BLOCKED | Could not operate the app or observe the terminal state. |
| AC-04 — rotation/background continuity and undo | BLOCKED | Could not operate the app or inspect state across lifecycle changes. |
| AC-05 — Android scope and reasonable Page 2 design comparison | BLOCKED | Build target was Android, but UI inspection and design comparison were unavailable; architecture boundaries are not observable through this black-box run. |

## Limits and attestation

CUA app inventory did not expose the running Android emulator as a controllable native app; selecting `Android Emulator` failed. ADB confirmed the device and installed APK, but it did not provide the required CUA-based visual interaction. This is an environment/tooling block, not a product PASS or FAIL. No source code or implementation details were inspected, and no product code was changed.

Final revision attestation: the installed APK hash above was built from the FE-F03 candidate working tree whose reported HEAD was the stated base revision. The uncommitted tree was not fingerprinted by this independent black-box test. No acceptance criterion is verified; live UI acceptance remains BLOCKED.

Review artifact: [20261113T155309Z.md](tests/reviews/20261113T155309Z.md).

## Follow-up — 2026-11-13 15:56 UTC

After PM authorization, ADB/UIAutomator was used directly. The emulator reported `emulator-5554`; package `ru.srastemsya.catalog` was installed. Launching its `MainActivity` displayed a page headed “Каталог компонентов Android” with component samples and a bottom navigation bar. “Лента” was already selected; tapping its bottom navigation position left the same component showcase visible. No sign-in or FE-F00/FE-F01 entry flow appeared. The visible page did not expose a single-device candidate feed. Screenshot: [20261113T155647Z-screen.png](tests/reviews/20261113T155647Z-screen.png).

This follow-up does not verify AC-01 through AC-04: candidate behavior, authenticated feed requests, result details, paging, exhaustion, or lifecycle continuity were not reached. AC-05 remains blocked because the showcase does not allow black-box verification of feature boundaries or the Page 2 flow. The installed app's available UI surface is the concrete obstacle; no further OTP attempt was made. Results remain BLOCKED, not PASS. No product code was changed or inspected.

## Follow-up — 2026-11-13 15:58 UTC

Following PM direction, tapped the visible “Продолжить выбор” CTA under “Комната и выбор”, then tapped the “Выбор места” sample row. UIAutomator continued to report the same “Каталог компонентов Android” showcase and no feed/filter/result content. The screenshots after each tap are [continue CTA](tests/reviews/20261113T155833Z-continue.png) and [place sample](tests/reviews/20261113T155833Z-place.png). Neither control reached the single-device feed. AC-01 through AC-04 remain BLOCKED/unverified; AC-05 remains BLOCKED for Page 2/architecture evidence. No OTP retry, implementation inspection, or product modification occurred.

## Final follow-up — 2026-11-13 16:13 UTC

### Corrected app identity

Package Manager diagnosis identified the correct candidate package. `aapt dump badging` reports the built APK as `fefu.prime.srastem`, version `1.0` / code `1`; it is installed at the corresponding `/data/app/.../fefu.prime.srastem` path. Its launcher is `fefu.prime.srastem/.MainActivity`. `ru.srastemsya.catalog` is a separate installed app; the earlier component-gallery observations were from that unrelated package and are superseded as candidate-app evidence. Candidate APK SHA-256 remains `3da6c2f880fb3eaf744706af9d8ca4088e9dbbbabd5e868e2e5a762f14f8f218`; source `HEAD` remained `50af9d72c70dc827ad798056e3e516799362aee5`, with uncommitted candidate changes as stated in the packet.

### Final criterion statuses

| Criterion | Result | Observed evidence |
|---|---|---|
| AC-01 — first LIKE and catalog details | **FAIL** | In place mode, first LIKE opened “Вы выбрали” with the correct title, parameters, description fallback, and “К месту”; in event mode, the action label was “К мероприятию”. Opening place details showed a different full catalog description (“Стендовый объект каталога.”), indicating the details view loaded fuller catalog content. However, the result cover panel and details cover panel were both blank white, while the selected feed card visibly displayed the illustrated fallback cover. The cover requirement therefore failed at the UI level. Cause (absent or temporary cover URL versus client rendering) cannot be determined from the public UI evidence. Screens: [feed card](tests/reviews/20261113T161109Z-feed.png), [place result](tests/reviews/20261113T161109Z-result.png), [place details](tests/reviews/20261113T161109Z-details.png), [event result](tests/reviews/20261113T161344Z-event-result.png). |
| AC-02 — automatic next page and ten-card pools | **PASS** | Feed counter advanced from `1 / 10` to `10 / 10`; after the first pool a new local `1 / 10` pool appeared. At the next server-page boundary, immediate screenshot showed “Обновляем подборку” and “Ищем новые варианты по вашим фильтрам”; swipe actions were absent during the request. The subsequent screenshot showed a new candidate and `1 / 10`. Filters/type were unchanged across the request. Screens: [loading state](tests/reviews/20261113T161109Z-refresh.png), [next-page pool](tests/reviews/20261113T161109Z-next-page.png). API request parameters were not packet-captured; automatic refresh, page content change, and unchanged filter context were observed in UI. |
| AC-03 — exhausted feed and retained filters | **PASS** | After the final candidate, UI displayed “Больше вариантов нет” and “Изменить фильтры”. The action returned to one-device place filters with Places selected, “Все”, no budget, party size `2`. Screens: [exhausted state](tests/reviews/20261113T161109Z-exhausted.png), [retained filters](tests/reviews/20261113T161109Z-retained-filters.png). |
| AC-04 — rotation/background continuity and undo | **PASS** | After SKIP to `2 / 10`, landscape rotation showed the same candidate; restoring portrait returned the same candidate and `2 / 10`. HOME then relaunching the existing task returned to that same state. Undo restored the previous candidate and `1 / 10`, demonstrating retained decision/undo history. Screens: [landscape](tests/reviews/20261113T161109Z-rotation.png), [background restore](tests/reviews/20261113T161109Z-background.png), [undo result](tests/reviews/20261113T161109Z-undo.png). |
| AC-05 — Android-visible scope/design | **PASS (visible scope only)** | The exercised artifact is Android package `fefu.prime.srastem`. The feed shows a layered card stack, place/event title bar, one-device context, counter/progress, swipe actions, and filter action, consistent with the supplied handoff and Page 2 flow description. Architecture and feature-documentation checks are assigned to the Developer checker; this black-box run makes no claim about those internal requirements. |

Final attestation: the tested build was the installed FE-F03 candidate APK identified above, running from `fefu.prime.srastem/.MainActivity`; no OTP retry was made. The only criterion failure observed is the blank result/details cover panel. No source code was inspected or changed by the Tester.

Review/evidence: [20261113T161109Z screenshots](tests/reviews/20261113T161109Z-next-page.png), [event result screenshot](tests/reviews/20261113T161344Z-event-result.png). Earlier preliminary statuses and wrong-package observations above are preserved for chronology; this section is the final candidate-build assessment.

## Targeted follow-up — 2026-11-13 16:32 UTC

Installed candidate identity: `fefu.prime.srastem/.MainActivity`, version `1.0` / code `1`; APK SHA-256 `5b875117a0c894306544c5b48b1dfc486bf702a86f457c030460103444a13161` (matches test packet). Reported HEAD remains `50af9d72c70dc827ad798056e3e516799362aee5`. Test used existing `emulator-5554`; no OTP attempt was needed.

| Criterion | Follow-up result | Evidence |
|---|---|---|
| AC-01 — first LIKE result and catalog details | **PASS for blank-cover fix** | Started one-device place flow, accepted default filters, and liked first candidate. “Вы выбрали” result showed the selected bookshop cover, title, parameters, description placeholder, and “К месту”. Opened details; the same cover and full catalog description (“Книжная лавка «Между строк». Стендовый объект каталога.”) appeared. Screens: [result](tests/reviews/20261113T163235Z-result.png), [catalog details](tests/reviews/20261113T163235Z-details.png). |
| AC-02–05 | Not retested in this follow-up | Prior run evidence remains listed above; this follow-up made no claim about those criteria. |

Review: [20261113T163235Z.md](tests/reviews/20261113T163235Z.md). No source files were inspected or modified.
