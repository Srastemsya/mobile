# FE-F04 test packet

- Base revision: `75af8e33b6b7eb4eed3998ca99b5e8798598870f`; uncommitted worktree `/Users/nixbi/Documents/GitHub/mobile/.worktrees/FE-F04`, branch `codex/FE-F04`.
- **Current retest Android APK:** `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `7180aa5aa8a5c7e2e1e64443a3e169037ed3fb260c459ad7a2ae8751f6cc8b3a` (base commit `4a290c7bd764e03113b940965f0cb8875fa5d2a9` plus uncommitted remediation).
- Public docs: `docs/specs/feed-v1/implementation.md`, `docs/specs/feed-v1/acceptance.md`, `docs/design/feed.md`, `docs/contracts/mobile-api.md`, supplied `/Users/nixbi/Downloads/srastemsya-frontend-api-2026-11-13.zip` (including `docs/contracts/lobby-v2.md`), `docs/android-testing-environment.md`.
- API base: `https://srastemsya.nixbi.ru`.
- Android target: `emulator-5554`, `fefu.prime.srastem/.MainActivity`; do not use catalog app. A prior session or `person1@example.test`, OTP `0427` may be used; honor cooldown.
- Build command: `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain`.

- AC-09: organizer starts through room filters; room response shows the pool count (up to 10 per supplied `docs/contracts/lobby-v2.md`). Room voting UI is outside this package.
- Developer build evidence: `workflow/reports/FE-F04/developer-build-20261113T170007Z.log`; host result `shared/build/test-results/testAndroidHostTest/TEST-fefu.prime.srastem.feed.presentation.RoomLobbyViewModelTest.xml` (5 passed).

- AC-09 correction build: room filter UI omits participant count; server derives it from connected membership. Single-device filters remain separate. Historical AC-09 APK SHA-256 `fd0eb39316f59ca141d2d5c80c4a24d85a4110994073712d8f0f88350692aa10`; immutable build log `workflow/reports/FE-F04/developer-build-ac09-20261113.log`.

## Tester retest addition — 2026-11-14

- The updated APK above was installed successfully on both `emulator-5554` and a second read-only instance `emulator-5556`; both reported the FE-F04 package activity. The filter/start/guest-poll scenario was rerun against this build.
- A separate authorized second-user fixture was provided for the run; credentials are intentionally omitted. Screenshots and a redacted run record are in `workflow/reports/FE-F04/evidence/`.
- The user explicitly excluded waiting for the 30-minute invite TTL. Live new-user expiry and post-expiry member rejoin remain untested; the synthetic unknown-token UI result is not treated as expiry evidence.

- AC-07/08 targeted retest APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `7180aa5aa8a5c7e2e1e64443a3e169037ed3fb260c459ad7a2ae8751f6cc8b3a`; base commit `4a290c7bd764e03113b940965f0cb8875fa5d2a9` plus uncommitted remediation. Developer build log: `workflow/reports/FE-F04/developer-build-visual-20261113T174029Z.log`.
