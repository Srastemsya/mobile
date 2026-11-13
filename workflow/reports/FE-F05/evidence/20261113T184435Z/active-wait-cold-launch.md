# Fixed-build active-wait cold-launch check

- Fixed APK SHA-256: `2bf903ed23a9888a6fe88efe6aef3d43fed7a262742c77689c98f23dd67ed81e` (local and installed base.apk on both devices matched).
- Package/activity: `fefu.prime.srastem/.MainActivity`, versionName `1.0`, versionCode `1`.
- Devices: `emulator-5554` and `emulator-5556`, Android 15/API 35, `sdk_gphone64_arm64`; existing `Srastemsya35` AVDs remained read-only.
- Fresh live room: `F05 wait test`; both clients observed round 1, 10 candidates, same first candidate.
- Before cold launch, P1 completed all 10 cards and saw `Ваш голос принят`, `Завершили 1 из 2`, and `Просмотреть свои голоса`. P2 was still at `Раунд 1 · 0/10` on the first candidate.
- Action: force-stopped P1 and cold-launched `.MainActivity` directly. No invite link was opened or used.
- After launch, P1 showed the same room, `Раунд 1 · 10/10`, `Ваш голос принят`, `Завершили 1 из 2`. P2 remained at `Раунд 1 · 0/10` on the first candidate.
- No data reset/clear. No credentials, OTP, invite URI, or sensitive payloads are present.

Screenshots: `p1-wait-restored.png` (restored wait state) and `p2-voting-active.png` (the unfinished member).
