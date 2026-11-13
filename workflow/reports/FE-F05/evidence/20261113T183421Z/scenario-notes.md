# FE-F05 live Android scenario evidence

- Revision: `ff576726f40c1d3b1dd138794c51d2287df845f0` (checkout had modified files; implementation source and diffs were not inspected).
- Installed APK on both emulators: SHA-256 `71059a0fec6d8753e60a9c59dd0457f38c147e7987e4e1db8b3600d82dc2666d`, package `fefu.prime.srastem`, versionName `1.0`, versionCode `1`.
- Devices: `emulator-5554` and `emulator-5556`, Android 15/API 35, product/model `sdk_gphone64_arm64` / `sdk_gphone64_arm64`. Both ran the existing `Srastemsya35` AVD read-only; no reset or app-data clear.
- Test room: `F05 testing`; live API only. Synthetic accounts were used for sign-in; credentials and OTP are intentionally omitted.

## Observed flow

1. Both members entered the same room and saw the same first round title and order. Round 1 presented 10 candidates; both saw `Книжная лавка «Между строк»` first and `Оранжерея «Зелёный двор»` second. The organizer liked the first card, used `Отменить выбор`, revisited it, then passed. The guest remained at 0/10 on the first card until voting independently.
2. Organizer passed all ten, completed the round, and saw `Ваш голос принят`, `Завершили 1 из 2`, and `Просмотреть свои голоса`. Guest still showed 0/10 and the first card. Review displayed the organizer's own first-card `SKIP` and returned to waiting without changing the state.
3. After force-stop and relaunch, the app opened Home. Re-entering the live room through its invite link restored the waiting snapshot (`Ваш голос принят`, 1/2). No other member's votes appeared.
4. Guest passed card 1, liked card 2, passed cards 3–10, and completed. Neither candidate reached 70% (two-member threshold requires both likes); both clients advanced to round 2, showing `Кафе у моря` first with a refreshed expiration. No round-1 candidate was duplicated in the observed round-2 pool.
5. Both members liked round-2 card 1 (`Кафе у моря`), passed the other nine candidates in the same order, and finished. Both screens converged to `Матч большинства`, `2 из 2 выбрали этот вариант`, and `PLACE · dev-vlad-cafe`. `К месту` opened `Кафе у моря` details (`Кафе у моря. Стендовый объект каталога.`, Владивосток, 750 ₽).

## Evidence images

- `match-emulator-5556.png`: match result on the second member.
- `place-details-emulator-5554.png`: matched place detail opened from the result action.

The invitation URI, account identifiers, and OTP are not present in this file or the screenshots.
