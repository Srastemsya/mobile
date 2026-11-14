# Test packet — 14 room place

Revision: `codex/ui-14-room-place`, based on `729ebac`; final commit recorded in Git history. Android debug APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `887af19ed406f2c938024f7d5c14d605b4a7bdd6c84cea729e8c0c23bff7a987`.

Public references: [screen 14](../../../../docs/specs/swipe-ui/14-room-place.md), [shared swipe UI](../../../../docs/specs/swipe-ui/README.md), [acceptance](acceptance.md), [design](../../../../docs/design/feed.md#экран-14--карточка-места-в-комнате).

Build command: `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain`.

Fixture access: follow the documented Android environment in [android-testing-environment.md](../../../../docs/android-testing-environment.md). Enter a room, start a place round, and use the same room on at least two member sessions to observe voting and the resulting room phase.
