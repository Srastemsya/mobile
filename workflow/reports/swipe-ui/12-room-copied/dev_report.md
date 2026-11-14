# Developer report — 12-room-copied

- Revision: `codex/ui-10-room-create`, base `0e72b5aa189e77044ac7caaae95513fe5cb70272`; scoped commit recorded in PM handoff.
- Build: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `593d2d98dcea1364e5d969e0b43afc94cb3e68e894ce3b23149ebcc99c6f2929`.
- Public spec: [12-room-copied](../../../../docs/specs/swipe-ui/12-room-copied.md); design: [feed](../../../../docs/design/feed.md).
- Changes: Copy writes the invite URL to the clipboard and replaces the card contents with the compact UI Kit copied state; the filters action remains below.
- Rules: reused UI Kit controls/tokens and existing KMP presentation/use-case boundaries; updated `feed/FEATURE.md` and design documentation. No acceptance file was changed.
- Checks: `:androidApp:assembleDebug :shared:testAndroid` passed; `git diff --check` passed. Debug APK installed on `emulator-5554`. Runtime baseline: [sign-in gate](../10-room-create/runtime-start.png).
- Limitations: Runtime copy and continuation were not observed because the emulator had no authenticated fixture.
