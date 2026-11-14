# FE-F02 developer report

- Base/revision: `a67098fea02e445bdb3f64d85407fcf9a3b3933d` (`dev`); working tree changes are uncommitted.
- Scope: Android/shared Compose swipe presentation only. iOS and backend behavior were not changed.

## Changes

- Added `FeedSwipeState` for pool position, local LIKE/SKIP decisions, undo and progress.
- Added swipe/undo events and bound feed candidates to the swipe state in `FeedModeViewModel`.
- Replaced the candidate title list with a layered `SrVoteCard` view, metadata, project artwork fallback, counter, progress/actions, and horizontal drag decisions in `FeedModeScreen`.
- Updated the feed `FEATURE.md` with FE-F02 behavior and limits.
- Added Android `ActivityStateHolder` so manually assembled shared ViewModels, including `MainViewModel` and `FeedModeViewModel`, survive Activity recreation. Guarded `NavigationMain` feed-entry initialization so it does not reset an existing session on rotation.
- Adapted the card layout to landscape with a compact card and vertically reachable content; added directional slide animation to swipe gestures and action buttons. When `loadingNextPool` is set, the card/actions are replaced by a visible loading state.
- Split fetched candidates into local pools of at most 10; overflow remains queued, is activated in order at the boundary, and starts with position/progress and undo history reset. Added focused common tests for 20-card and short-overflow splits.
- Replaced raw category identifiers in card metadata with the loaded category label (or a localized kind fallback), and constrained landscape cards to a compact scrollable viewport so title, metadata and description can be reached.
- Added a plain, high-contrast text line beneath the card using only available category/price/address values, so metadata remains visibly readable if styled pills render without text. Empty category IDs use the kind label; absent price/address are omitted, with no fabricated values.
- Added a controlled delayed-boundary test: `loadingNextPool` remains active and ignores decisions until the pending pool completion signal, then exits loading. UI branch uses that state to hide vote actions and show progress feedback.

## Evidence and checks

- Source review: uses `:uikit` `SrVoteCard`, `SrVoteActions`, `SrTopBar`, `SrArtwork` and existing theme tokens; no platform-specific imports or card-tap navigation added.
- API source review: Android `backendBaseUrl()` is `https://srastemsya.nixbi.ru` in `shared/src/androidMain/.../Platform.android.kt`; `BackendApi` uses it by default, and `AppContainer` wires `GetFeedCandidatesUseCase` to `BackendFeedRepository`. No local backend or mock feed repository is wired by the Android app.
- Command: `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain`
- Result after metadata visibility and delayed loading-state test updates: `BUILD SUCCESSFUL`; Android debug APK assembled at `androidApp/build/outputs/apk/debug/androidApp-debug.apk`; `:shared:testAndroid` passed, including ten-card boundaries, short overflow, and delayed loading decision suppression. An initial sandboxed attempt hit `java.net.BindException`; the approved retry succeeded.

## Limitations

- Room context label is modeled in state but FE-F04 does not currently populate it; no room screen flow was added.
- The FE-F03/FE-F05 pool-loading boundary is represented by `loadingNextPool`, but no subsequent-pool fetching is implemented in this FE-F02 task.
- Tester confirmed rotation retention on the prior AC-09 APK. AC-09 client-side pool split is covered by `FeedSwipeStateTest`; backend page-size behavior was not changed. Landscape/readability fixes have received a black-box recheck; this latest explicit metadata text adjustment awaits recheck.
- Tester confirmed rotation retention on the prior AC-09 APK. On the latest APK, metadata pill text still appeared blank in portrait although accessibility exposed the values; this patch adds the explicit high-contrast metadata line. Staging candidate provenance remains unverified because OTP verification returned 422 and subsequent throttling prevented access; no local backend was used.

## Public documentation

- [Feed implementation specification](../../../docs/specs/feed-v1/implementation.md#fe-f02--свайп-экран-и-стопка-карточек)
- [Feed feature documentation](../../../shared/src/commonMain/kotlin/fefu/prime/srastem/feed/FEATURE.md)
