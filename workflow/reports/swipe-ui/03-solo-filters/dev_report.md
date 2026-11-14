# Developer report — 03 Общие фильтры

- Revision: `codex/ui-02-device-choice`, based on `0e72b5aa189e77044ac7caaae95513fe5cb70272`.
- Android build: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`; SHA-256 `97f4446d0e80a7c43cc97691701723de51c7bae6fab24eec57ed91fe64e3ccac`.
- Changes: added «Тип подборки» label, reordered date after budget, retained date only for events, matched helper text punctuation, removed the extra visible participant field, and changed the primary action to «Начать выбор». The preselected Events kind and retained filter state use the existing ViewModel flow.
- Public docs: [spec](../../../../docs/specs/swipe-ui/03-solo-filters.md), [design](../../../../docs/design/feed.md), [feature](../../../../shared/src/commonMain/kotlin/fefu/prime/srastem/feed/FEATURE.md).
- Verification: `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain` — passed. `git diff --check` — passed. Architecture source review: Compose UI uses existing UI Kit chips, fields, top bar, button, colors and typography. No new networking or domain logic was added.
- UI limitation: the emulator opens on sign-in and no verified profile-complete staging fixture is available in the public setup. Screen 03 state changes and immediate transitions could not be captured; AC-01 through AC-05 remain unverified on device. See [entry evidence](../02-device-choice/emulator-launch.png).
