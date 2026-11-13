# FE-F05 cold-launch and room re-entry check

- Build: packet APK SHA-256 `71059a0fec6d8753e60a9c59dd0457f38c147e7987e4e1db8b3600d82dc2666d`, installed hash matched on both emulators.
- Device: `emulator-5556`, read-only existing AVD, no data clear/reset.
- Action: `am force-stop fefu.prime.srastem`, then start `fefu.prime.srastem/.MainActivity`.
- Observed after cold launch: Home screen (`Куда срастёмся сегодня?`, Feed, Events, Profile), not the current room/match.
- Re-entry: opening the existing invite link returned the live terminal result: `Матч большинства`, `2 из 2 выбрали этот вариант`, `PLACE · dev-vlad-cafe`.
- Invite URL, account identifiers, and OTP are omitted.
- The 30-minute expiration was not waited for; parent explicitly excluded that wait. No controlled stale-response or error condition was induced.
