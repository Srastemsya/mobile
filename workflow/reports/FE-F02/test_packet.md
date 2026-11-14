# FE-F02 test packet

- Revision: base `a67098fea02e445bdb3f64d85407fcf9a3b3933d`, uncommitted working tree.
- Public behavior: see [FE-F02 implementation specification](../../specs/feed-v1/FE-F02.md), [feed implementation](../../../docs/specs/feed-v1/implementation.md#fe-f02--свайп-экран-и-стопка-карточек), and [feed design](../../../docs/design/feed.md).
- Android build and host test command: `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain`.
- Staging API: `https://srastemsya.nixbi.ru` (do not use local backend). UI fixtures are provisioned through the approved tester environment. The screen uses candidates returned by the existing feed path.
- The local pool boundary is 10 cards. API response/page size remains governed by its public contract (up to 20); client overflow is preserved and tested by `FeedSwipeStateTest`.
- Android feed is wired to staging through `backendBaseUrl()` and `BackendFeedRepository`; candidate data was not inspected because tester OTP auth failed. No local backend is used.
- Build/test after metadata visibility and delayed loading-state test completed with `BUILD SUCCESSFUL`; `:androidApp:assembleDebug` and `:shared:testAndroid` passed. `FeedSwipeStateTest` covers 10-card split, preserved overflow, reset progress and ignored decisions while a controlled deferred pool request is pending. New APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`; request black-box retest of metadata visibility.
