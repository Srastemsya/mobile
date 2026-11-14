# Test packet — 04 solo place

## Revision and build

- Branch: `codex/ui-04-solo-place`; base revision: `96fc48e53aeb434f7b9b3ca3405a50c93b3c6674`.
- APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- APK SHA-256: `f0c81b9deb2566771d9607c647e6fc929a367ebdcc3703eb5abdf5a899f444ae`.

## Public docs and interfaces

- [Screen 04 specification](../../../../docs/specs/swipe-ui/04-solo-place.md)
- [Swipe UI flow and frame links](../../../../docs/specs/swipe-ui/README.md)
- [Feed design and user route](../../../../docs/design/feed.md)
- [Android test environment](../../../../docs/android-testing-environment.md)
- Build command: `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain`.
- ADB command: `/private/tmp/srastemsya-sdk.7DCrTs/platform-tools/adb devices`.

## Fixture access

- Screen 04 is entered from Home → single device → filters with Places selected and at least one candidate in the response.
- No dedicated candidate fixture or configured emulator was available in this developer run. The local AVD list was empty; ADB listed a physical device that was not changed.
