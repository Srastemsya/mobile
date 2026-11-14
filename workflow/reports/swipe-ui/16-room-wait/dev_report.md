# Developer report — 16-room-wait

- Revision: `codex/ui-16-17-room-states`, based on `9ecd43b3c24d05cbd865b7946d28677882d1123a`.
- Build: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `6a56cdaec0adcf2114bd3cf5c675cf4e4ab1c3a287ea6d7d4fab07e23c64c6aa`.
- Public spec: [16-room-wait](../../../../docs/specs/swipe-ui/16-room-wait.md); design: [feed](../../../../docs/design/feed.md#экраны-1617--ожидание-и-общий-результат).
- Changes: After a completed room vote, the waiting view shows the specified heading, explanation, status card, and return action. The room observation remains active, so server phase changes select the next view automatically. The previous waiting markup and review controls are removed from the room lobby.
- Rules: UI Kit top bar, card, button, icon and theme tokens are reused; the existing `RoomLobbyViewModel` and room use cases keep state and data access inside their established layers. Updated the feed `FEATURE.md` and public design description. Acceptance criteria were not changed.
- Checks: `:androidApp:assembleDebug :shared:testAndroid` passed on the local Android SDK; `git diff --check` passed. Initial sandboxed Gradle invocation could not bind its local lock socket; the rerun with build permission passed.
- Limitation: Screen transition needs the separately assigned room swipe screens 14/15 and a server room result for runtime verification. No PASS claim for acceptance.
