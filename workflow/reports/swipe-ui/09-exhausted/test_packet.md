# Test packet — 09-exhausted

- Public specification: `docs/specs/swipe-ui/09-exhausted.md`; acceptance: `workflow/reports/swipe-ui/09-exhausted/acceptance.md`; design: `docs/design/feed.md`.
- Reference frame: see public specification; 390 × 844 viewport.
- Build identity: branch `codex/ui-06-09-solo-states`, Android debug APK `androidApp/build/outputs/apk/debug/androidApp-debug.apk`, SHA-256 `028114b355d0c22c8d4f12122a648c3fad74ef38dd1df03355b6ea07a88501d5`.
- Build command: `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroid --no-configuration-cache --console=plain`.
- Runtime fixture: authenticated completed profile, candidate feed with selected item or next-page cursor as described in the public specification.
