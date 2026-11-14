# Test packet — 05 solo event

- Revision: branch `codex/ui-05-solo-event`, based on `96fc48e53aeb434f7b9b3ca3405a50c93b3c6674`.
- Android APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`; SHA-256 `24d1d2be234af68757725383e1b4dc196b4a3a5821c883c3feebe5fd53c72e66`.
- Public behavior: [screen 05](../../../../docs/specs/swipe-ui/05-solo-event.md), [full swipe UI route](../../../../docs/specs/swipe-ui/README.md), [design handoff](../../../../docs/design/feed.md), [reference frame](https://www.figma.com/design/khewsPf7b4JVyBwJaCmO43/startap_design?node-id=280-10862).
- Entry: from «Открытия», open swipe selection, choose one device, select «События» in filters, then start selection. Use a backend account with a complete profile and event candidates; no fixed event fixture is bundled.
- Build command: `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug --no-configuration-cache --console=plain`.
