# Developer report — 13-room-filters

- Revision: `codex/ui-10-room-create`, base `0e72b5aa189e77044ac7caaae95513fe5cb70272`; scoped commit recorded in PM handoff.
- Build: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `593d2d98dcea1364e5d969e0b43afc94cb3e68e894ce3b23149ebcc99c6f2929`.
- Public spec: [13-room-filters](../../../../docs/specs/swipe-ui/13-room-filters.md); design: [feed](../../../../docs/design/feed.md).
- Changes: The available legacy organizer frame now includes type and server-backed category chips, the existing total-budget input, an event-date picker, and the start action. Selected categoryId is sent through the existing start operation.
- Rules: reused UI Kit controls/tokens and existing KMP presentation/use-case boundaries; updated `feed/FEATURE.md` and design documentation. No acceptance file was changed.
- Checks: `:androidApp:assembleDebug :shared:testAndroid` passed; `git diff --check` passed. Debug APK installed on `emulator-5554`. Runtime baseline: [sign-in gate](../10-room-create/runtime-start.png).
- Limitations: BLOCKED: target per-participant submission, personal budget aggregation, waiting state, and route to the shared pool lack target design and backend contract. The current API still starts with organizer-owned total budget. The UI therefore retains the honest “Общий бюджет, ₽” label, differing from the frame. Runtime state was not observed because the emulator had no authenticated fixture.
