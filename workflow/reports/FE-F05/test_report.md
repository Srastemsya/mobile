# FE-F05 independent black-box acceptance report

- **Overall:** PARTIAL — AC-01 through AC-03 passed in the focused live scenario on the original packet APK. The fixed APK passed the active-wait cold-launch restore check for AC-04. The 30-minute closure wait was excluded per instruction. AC-05 has live-update evidence plus PM-reported focused test evidence; live transport fault and poll lifecycle cases were not independently induced.
- **Git HEAD:** `ff576726f40c1d3b1dd138794c51d2287df845f0` (worktree modified; no application source, developer report, or implementation diffs were inspected).
- **Functional scenario APK:** `71059a0fec6d8753e60a9c59dd0457f38c147e7987e4e1db8b3600d82dc2666d`.
- **Fixed-build restoration APK:** `2bf903ed23a9888a6fe88efe6aef3d43fed7a262742c77689c98f23dd67ed81e`; installed base APK hash matched on both devices. Package/activity `fefu.prime.srastem/.MainActivity`, versionName `1.0`, versionCode `1`.
- **Devices:** `emulator-5554` and `emulator-5556`, Android 15/API 35, `sdk_gphone64_arm64`. Existing `Srastemsya35` AVDs ran read-only; no reset or app-data clear.
- **API:** live `https://srastemsya.nixbi.ru`, no mocks/local backend. Authorized synthetic accounts were used; identifiers and OTP omitted.
- **Launch metadata:** APK manifest declares `fefu.prime.srastem.MainActivity`. The original packet launcher spelling `fefu.prime.sr/.MainActivity` returned Activity class not found; the manifest-declared component was used.

## Scenario results

On the original packet APK, a two-member live room presented the same 10-card first-round order on both devices. P1 liked card 1, used the visible undo control to revisit it, then changed it to PASS. P2 remained on card 1 at 0/10 while P1 progressed. P1 passed all ten, finished, and saw `Ваш голос принят`, `Завершили 1 из 2`, and a review action. Review showed only P1's own first-card `SKIP`; returning did not change waiting. P2 passed card 1, liked card 2, passed the rest, and finished. The server advanced both clients to round 2 because one like among two members missed the 70% threshold. Both saw the same new pool order and expiration; no observed card from round 1 repeated.

Both members then liked round-2 card 1 and passed the remaining nine in the same order. After both finished, both showed `Матч большинства`, `2 из 2 выбрали этот вариант`, and `PLACE · dev-vlad-cafe`. `К месту` opened `Кафе у моря` details (description, Владивосток, 750 ₽).

On the fixed APK, a fresh live room `F05 wait test` was observed on both clients before process termination. P1 completed all ten cards and showed the waiting acknowledgement and 1/2 completion; P2 remained at 0/10 on the first card. P1 was then force-stopped and `.MainActivity` launched directly, without opening or using the invite. It restored the same room, round 1, 10/10, waiting acknowledgement, and 1/2 completion. The required 30-minute expiry/closed-state wait was not run per instruction.

## Acceptance criteria

| Criterion | Verdict | Build and evidence |
|---|---|---|
| AC-01 Shared round and personal votes | PASS | Original packet APK `71059a…2666d`: two shared 10-card rounds, same order, P1 like→undo→pass, own-vote review, P2's independent 0/10 snapshot. Full run details: [scenario notes](evidence/20261113T183421Z/scenario-notes.md). |
| AC-02 Match threshold and result | PASS | Original packet APK `71059a…2666d`: 1/2 first-round likes did not match; 2/2 second-round likes yielded the same winner and count metadata on both clients; place action opened the winner. [Match screenshot](evidence/20261113T183421Z/match-emulator-5556.png). |
| AC-03 No threshold and next round | PASS | Original packet APK `71059a…2666d`: no-threshold split votes advanced both clients to round 2 with a fresh pool and expiration, no observed duplicate. [Scenario notes](evidence/20261113T183421Z/scenario-notes.md). |
| AC-04 Wait, restore and close | PARTIAL | Fixed APK `2bf903…ed81e`: active unfinished waiting state restored after force-stop/cold launch without invite. Wait/own-vote review also observed in the original run. The 30-minute expiry, CLOSED state, disabled voting, and exit were not run per instruction. [Cold-launch evidence](evidence/20261113T184435Z/active-wait-cold-launch.md). |
| AC-05 Live API resilience | PARTIAL | Live clients received the other member's completion and round-2 update; fixed APK restored the fresh waiting snapshot. PM reports latest `:shared:testAndroid` passed and covers stale observed-version rejection, `INVALID_STATE` snapshot reread, and `STALE_ROUND`→`InvalidState` mapping ([PM-referenced log](/tmp/fe-f05-ac05-tests.log)); that evidence was not independently inspected. No live stale/error replay or leave-and-resume polling scenario was induced. |
| AC-06 Android usability/design | PASS (focused usability) | Voting, waiting, refresh, match, and detail text were readable; controls were actionable. Match and place detail screenshots: [match](evidence/20261113T183421Z/match-emulator-5556.png), [details](evidence/20261113T183421Z/place-details-emulator-5554.png). This was not a full Figma frame-by-frame comparison. |

## Timestamped evidence

- [Original packet APK scenario notes](evidence/20261113T183421Z/scenario-notes.md)
- [Fixed APK active-wait cold-launch evidence](evidence/20261113T184435Z/active-wait-cold-launch.md)
- [Fixed APK restored waiting screen](evidence/20261113T184435Z/p1-wait-restored.png)
- [Fixed APK unfinished participant screen](evidence/20261113T184435Z/p2-voting-active.png)
- Earlier adb availability attempts are preserved: [initial check](evidence/20261113T180525Z/environment-check.md), [resumed check](evidence/20261113T180630Z/adb-device-check.md).

This was a manual black-box device run; no automated UI suite was invoked. Credentials, OTPs, invite URIs, and sensitive payloads are absent from the evidence. The earlier terminal-match cold-launch check is inconclusive for AC-04; the active-wait check above is the valid restore result.
