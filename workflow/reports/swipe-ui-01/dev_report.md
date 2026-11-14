# Developer report — swipe-ui-01

## Revision and build

- Worktree: `/private/tmp/mobile-ui-01`; branch: `codex/ui-01-discovery`; base HEAD: `049a764` (`dev`). Changes are uncommitted for PM review.
- Latest build: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, produced 2026-11-14; SHA-256 `3b6c8f32bf044a360f3121e5395ba1ab6f9fdef186885af5872e817c268c9c4c` (round 2).

## Implementation

- Adjusted the Discovery content's top spacing by 20 dp so its header, welcome panel, quick tiles and popular cards align with the 390 × 844 reference composition after the status inset.
- Hid the bottom tab bar as soon as `Destination.FeedScreen` opens. The existing full-tile click handler calls `MainViewModel.navigateToFeedWithEventsKind()`, which opens the device-choice screen and carries the Events selection forward.
- Documented the Home feature and updated the Home design and Feed feature docs to reflect the transition.

## Acceptance and rule evidence

- AC-01: `HomeScreen` renders the logo, bell, three-line greeting, two quick actions, blue swipe tile with card stack and arrow, and horizontal popular cards using UI Kit assets and `SrColors`/`SrType`. The new top inset matches the reference's welcome-panel start at about y=117 after a 24 dp status bar. `BottomNavigationBar` selects `SrTab.Home` for `Destination.MainScreen`.
- AC-02: `SwipeEventsAction` applies one `clickable` to the clipped full-size 92 dp tile. The text is placed before non-clickable artwork; artwork is offset to the right, leaving the caption and arrow readable. No child click handler intercepts the tile.
- AC-03: `NavigationMain` passes `navigateToFeedWithEventsKind` as `onOpenFeed`; `Destination.FeedScreen` renders `FeedModeScreen` (device-choice state) and now hides the tab bar at entry. No screen-02 layout changes were made.
- AC-04: `HomeScreen` owns the vertical `rememberScrollState`; `BottomNavigationBar` is a sibling overlay aligned to the bottom in `MainNavigation`, outside that scroll container.
- Architecture: no new network, domain or ViewModel behavior; the change uses existing navigation and UI Kit tokens/assets. `home/FEATURE.md` now covers the feature's domain, data and presentation interfaces and dependencies; `feed/FEATURE.md` reflects the route behavior.
- Design: `docs/design/home.md` records the full-tile action, fixed tab bar, next screen and specification links. Existing Figma frame references remain current.
- Interface changes: device-choice (`Destination.FeedScreen`) now renders without the tab bar. Existing callbacks and public method signatures are unchanged.

## Checks

- `git diff --check` — passed.
- `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroid --offline --no-configuration-cache --console=plain` — passed (`BUILD SUCCESSFUL`); 46 host tests, 0 failures/errors/skips across 10 suites. The initial sandbox run could not bind Gradle's local lock service; the permitted rerun completed.
- `adb devices -l` — no attached devices. A device screenshot and interaction check were therefore not completed by Developer.

## Limitations

- Visual alignment is source-estimated against the PNG; it has not been screenshot-compared on a 390 × 844 emulator in this worktree.
- Popular card data comes from `/v1/home`; empty/loading/error responses retain their existing states and cannot show the reference's dynamic cards until content arrives.
- The device-choice screen's internal controls and profile gate are existing behavior outside this screen's scope.

## Public documentation

- [`home/FEATURE.md`](../../../shared/src/commonMain/kotlin/fefu/prime/srastem/home/FEATURE.md)
- [`feed/FEATURE.md`](../../../shared/src/commonMain/kotlin/fefu/prime/srastem/feed/FEATURE.md)
- [`docs/design/home.md`](../../../docs/design/home.md)
- [`docs/specs/swipe-ui/01-discovery.md`](../../../docs/specs/swipe-ui/01-discovery.md)

## Round 2 — response to independent UI check

The Tester observed `UNAUTHENTICATED` in the popular section, so AC-01 failed and AC-04 had no scrollable content. The app's Android API origin is `https://srastemsya.nixbi.ru`; its `/health` returned HTTP 200, and a request to `/v1/home` without a token returned HTTP 401. This is a protected route. The documented local dev API at `127.0.0.1:8080` was not running (`curl` connection refused), and the build does not currently point to it. Thus an authenticated, profile-complete session against the configured API, whose `/v1/home` response contains collections, is required for a normal-state card screenshot. No card content was hard-coded or invented.

There was also an app-side startup race: `HomeViewModel` fetched `/v1/home` in its constructor while `RegistrationViewModel` was still restoring/validating the session. `NavigationMain` already calls `homeViewModel.refresh()` after registration finishes, so the early request could fail with 401 and compete with auth refresh. Round 2 removes that premature fetch; the Home request is now issued after registration completion. A new `HomeViewModelTest` verifies no request before explicit refresh and successful collection state after refresh. This fixes a candidate behavior that could produce the observed error, though the Tester's exact token state was not available for definitive root-cause attribution.

With two 223 dp cards and the reference's vertical layout, the previous 96 dp trailing spacer left the 844 dp feed barely scrollable or stationary. Round 2 increases that trailing clearance to 180 dp, keeping the initial composition unchanged while allowing a vertical swipe to move the card row above the fixed tab bar.

Round 2 checks: `git diff --check` passed; `:androidApp:assembleDebug :shared:testAndroid` passed with 47 tests, 0 failures/errors/skips across 11 suites. Latest APK SHA-256 is above. The authenticated UI result still requires independent rerun on a session with access to `/v1/home`; this report does not claim AC-01 or AC-04 passed visually.
