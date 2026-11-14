# Test packet — 03 Общие фильтры

- Revision: `codex/ui-02-device-choice`, based on `0e72b5aa189e77044ac7caaae95513fe5cb70272`.
- Build: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`; SHA-256 `97f4446d0e80a7c43cc97691701723de51c7bae6fab24eec57ed91fe64e3ccac`.
- Public behavior: [screen 03](../../../../docs/specs/swipe-ui/03-solo-filters.md), [shared scenario](../../../../docs/specs/swipe-ui/README.md), [acceptance](acceptance.md), [design](../../../../docs/design/feed.md).
- Launch: install the APK and open `fefu.prime.srastem/.MainActivity` on a 390 × 844 Android emulator. Entry is «Открытия» → «Свайпать события» → «На одном устройстве». Android testing setup: [environment](../../../../docs/android-testing-environment.md).
- Fixture access: a profile-complete account on the configured backend is required. The public dev fixture is documented in [backend integration](../../../../docs/backend-integration.md); its availability on staging is unconfirmed.
