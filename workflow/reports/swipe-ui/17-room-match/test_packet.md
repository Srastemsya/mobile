# Public test packet — 17-room-match

- Revision: `codex/ui-16-17-room-states`, based on `9ecd43b3c24d05cbd865b7946d28677882d1123a`.
- APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `6a56cdaec0adcf2114bd3cf5c675cf4e4ab1c3a287ea6d7d4fab07e23c64c6aa`.
- Screen specification: [17-room-match](../../../../docs/specs/swipe-ui/17-room-match.md); [shared UI guidance](../../../../docs/specs/swipe-ui/README.md); [design description](../../../../docs/design/feed.md#экраны-1617--ожидание-и-общий-результат); [acceptance](acceptance.md).
- Android test environment: [commands and SDK](../../../../docs/android-testing-environment.md). Entry: sign in with a complete profile, open **Свайпать события** → **На нескольких устройствах**, join a room and complete voting until a room result is available. An authenticated room-result fixture is not supplied in this worktree.
