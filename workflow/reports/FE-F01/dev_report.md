# FE-F01 Developer report

- Base revision: `0092b0ba38d9a3813d558454154ce075ec71cd82` (working tree includes the assigned FE-F01 implementation and this visual adjustment).
- Current source identity: `HEAD=0092b0ba38d9a3813d558454154ce075ec71cd82`; working-tree SHA-256 — `FeedModeScreen.kt`: `dacf6fa66993b8b80b68b3e6045051b4643b36028d81297462efb324a472c9d6`; `FeedModeViewModelTest.kt`: `3d35ad10b6e72067047f007e056735af58dabe50bfd6e38293a0e6f47c98408b`.
- Scope: Android validation requested; no iOS claim.

## Changes

- Filter screen `SrTopBar` now reads «Фильтры».
- Category options, including «Все», use Compose `FlowRow` with UI Kit `SrChip` instances, wrapping onto subsequent lines as available width requires.
- Kept the acceptance copy «Показать подборку».
- Updated `feed/FEATURE.md` to describe the wrapping category chips and filter title/button.
- Added `FeedModeViewModelTest` coverage for filter composition (kind, category, event date, rubles-to-minor-unit conversion, party size), PLACE date omission, invalid budget/party-size submission blocking, and restored filter values followed by a fresh updated query.
- Added commonTest data-layer HTTP query serialization assertions via `FeedContract.candidatesQuery`: EVENT sends `/v1/feed/candidates` with kind, categoryId, ISO `YYYY-MM-DD` localDate, budgetTotalMinor in RUB minor units, and partySize; PLACE serializes kind only when optional filters are null, omitting localDate and the other absent parameters. Existing query coverage also checks cursor encoding.

## Rule compliance

- Reuses `SrTopBar`, `SrChip`, existing color/type tokens, and existing screen layout; adds no color, typography, or dimension styling tokens.
- FE-F01 acceptance copy and server boundary are unchanged.

## Checks

- `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroidHostTest --no-configuration-cache --console=plain` — BUILD SUCCESSFUL after adding HTTP query serialization cases.
- `git diff --check` — passed.
- `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./tests/shared/run.sh` — exit code 1; harness report: [20261114-001419 shared suite](../../../tests/reports/20261114-001419-shared.md). The harness command failed before test execution because Gradle could not create `FileLockContentionHandler` (`java.net.BindException: Operation not permitted`); 0 passed, 0 failed, 0 skipped. Harness also invokes its fixed Android + iOS Simulator targets; no iOS result was obtained.
- Backend origin inspection: `shared/src/androidMain/kotlin/fefu/prime/srastem/Platform.android.kt` implements `backendBaseUrl()` as the hardcoded `https://srastemsya.nixbi.ru`. There is no Gradle property, environment variable, build variant field, or runtime override consumed by this function. Consequently there is currently no supported runtime/Gradle configuration to point FE-F01 at the local fixture. The emulator address would be `http://10.0.2.2:18090`, but using it requires an explicit source/build-configuration change; no server URL was changed here.

## Public references

- [FE-F01 implementation](../../../docs/specs/feed-v1/implementation.md#fe-f01--фильтры-подборки)
- [Feed feature documentation](../../../shared/src/commonMain/kotlin/fefu/prime/srastem/feed/FEATURE.md)
