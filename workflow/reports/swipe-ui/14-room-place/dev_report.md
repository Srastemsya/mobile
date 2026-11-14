# Developer report — 14 room place

Revision: `codex/ui-14-room-place`, based on `729ebac`; final commit recorded in Git history. Android debug APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `887af19ed406f2c938024f7d5c14d605b4a7bdd6c84cea729e8c0c23bff7a987`.

## Changes

- Screen 14 uses the shared `FeedSwipeCardStack` and UI Kit vote card. The room title and participant avatars sit above the stack; the counter and direction hint are removed. The owner alone sees a 48 dp filter control that displays the active room filters.
- Completed left/right swipes submit one room decision. The shared gesture resets on cancellation and guards against repeated submission during the exit animation. The room ViewModel completes the round after the final saved vote, allowing snapshot navigation to waiting 16 or match 17. A retry action remains for a failed completion request.
- Updated feed feature and design documentation; added a ViewModel test for one final vote and one completion.

## Checks

- `:androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain` — PASS, 95 tasks.
- `python3 tests/shared/check-feed-architecture.py` — PASS.
- `git diff --check` — PASS.
- `python3 tests/shared/check-feed-f03-architecture.py` — FAIL on existing source tokens in `FeedModeScreen.kt` (`SrVoteCard`, `SrVoteActions`, `FeedCover(...)`, `AsyncImagePainter.State.Error`). That script reflects earlier card markup and was not changed for screen 14.

## Limits

Build and host tests do not establish visual or gesture runtime acceptance. Initial frame, left/right, quick repeat, cancellation, and destination evidence remain for independent UI verification. The room API fixes filters at start, so the voting screen exposes active filters for owner inspection only; it does not edit an in-progress round.

Public references: [screen 14](../../../../docs/specs/swipe-ui/14-room-place.md), [acceptance](acceptance.md), [design](../../../../docs/design/feed.md#экран-14--карточка-места-в-комнате), [feature](../../../../shared/src/commonMain/kotlin/fefu/prime/srastem/feed/FEATURE.md).
