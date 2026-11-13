# FE-F03 Test Packet

## Revision and build identity

- Branch: `codex/FE-F03`; base revision: `50af9d72c70dc827ad798056e3e516799362aee5`.
- Candidate working tree is uncommitted. Debug APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`; SHA-256: `5b875117a0c894306544c5b48b1dfc486bf702a86f457c030460103444a13161`.

## Public behavior and API references

- FE-F03 behavior and acceptance: [specification](../../specs/feed-v1/FE-F03.md), [acceptance criteria](acceptance.md), [design handoff](../../../docs/design/feed.md).
- HTTP interfaces: `GET https://srastemsya.nixbi.ru/v1/feed/candidates` and `GET https://srastemsya.nixbi.ru/v1/catalog/{kind}/{id}`; see [Discovery v1 contract](../../../docs/contracts/discovery-v1.md) and [mobile API contract](../../../docs/contracts/mobile-api.md).
- Page size requested for the Android display is 10 cards. The server candidate API may return up to 20 candidates per response; the client splits each response into local groups of 10.

## Commands and fixture access

- Build and host tests: `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain`.
- Android SDK, AVD and install guidance: [Android testing environment](../../../docs/android-testing-environment.md).
- API fixture login: `person1@example.test`; one-time code: `0427`. The OTP service may rate-limit verification; attempt verification once only. If it rejects the code or indicates a limit, stop OTP attempts and record that live-auth-dependent checks are unavailable rather than retrying. Do not include credentials in execution reports.
