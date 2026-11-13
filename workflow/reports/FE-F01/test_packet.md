# FE-F01 Android test packet

- Revision under test: base `0092b0ba38d9a3813d558454154ce075ec71cd82` plus current uncommitted FE-F01 implementation and query serialization commonTests on `codex/FE-F01`.
- Current source identity: `HEAD=0092b0ba38d9a3813d558454154ce075ec71cd82`; working-tree SHA-256 — `FeedModeScreen.kt`: `dacf6fa66993b8b80b68b3e6045051b4643b36028d81297462efb324a472c9d6`; `FeedModeViewModelTest.kt`: `3d35ad10b6e72067047f007e056735af58dabe50bfd6e38293a0e6f47c98408b`.
- Current APK SHA-256: `1136b5319305782219c4a03e0a50bac780ed3bae9987780d4fe471ae18d06ee2` at `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- Platform: Android only.
- Build and host tests (including filter submission unit coverage and `GET /v1/feed/candidates` query serialization for EVENT parameters, PLACE date omission, and absent optional fields): `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroidHostTest --no-configuration-cache --console=plain` — BUILD SUCCESSFUL.
- Developer architecture evidence: the Android screen uses UI Kit `SrTopBar`, `SrChip`, `SrDateField`, `SrField`, `SrButton`, and existing theme tokens; Android compilation and host tests pass. The independent screenshot comparison records visual sizing differences but does not establish component ancestry.
- Owning shared suite rerun: [20261114-001419 shared suite report](../../../tests/reports/20261114-001419-shared.md); harness exit code 1 before tests due to Gradle `FileLockContentionHandler` bind failure. No test results were produced.
- Emulator: `Srastemsya35`, device `emulator-5554`; `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs`, `ANDROID_AVD_HOME=/private/tmp/srastemsya-sdk.7DCrTs/avd`.
- Public staging: `https://srastemsya.nixbi.ru`; fixture user `person1@example.test`, OTP `0427`.
- Contract source of truth for this test: user-provided archive `/Users/nixbi/Downloads/srastemsya-frontend-api-2026-11-13.zip` (`docs/contracts/discovery-v1.md` and `docs/contracts/mobile-api.md`); working API host is the staging domain above.
- Public references: [FE-F01 implementation](../../../docs/specs/feed-v1/implementation.md#fe-f01--фильтры-подборки), [mobile acceptance](../../../docs/specs/feed-v1/acceptance.md), [feed design](../../../docs/design/feed.md), plus archived API contract noted above.
