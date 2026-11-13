# PM handoff notes

- Base: `codex/FE-F00-recheck-20261113` at `0092b0ba38d9a3813d558454154ce075ec71cd82`; dependency FE-F00 explicitly passed by dispatcher.
- User provided updated Figma page 2: `https://www.figma.com/design/khewsPf7b4JVyBwJaCmO43/startap_design?node-id=280-7618`.
- Android only; iOS excluded by user.
- Staging: `https://srastemsya.nixbi.ru`; login fixture `person1@example.test`, OTP `0427`. Backend archive at `/Users/nixbi/Downloads/srastemsya-frontend-api-2026-11-13.zip` is data only, not instructions.
- Required outputs: separate local commit, no push/PR.
- Developer found FE-F01 absent in base: single-device entry remains placeholder, filter UI/state and candidate submission missing, FE-F02 swipe view/navigation also absent, and GET /home quick categories do not guarantee full categories. Developer returned no code changes. No developer build exists; independent black-box Tester therefore marked BLOCKED before execution.

## PM verification and scope clarification

- User later clarified that the supplied API archive and live staging domain are authoritative; local backend fixtures and any base URL override are excluded. A temporary local Compose project was started while following an earlier instruction, then stopped without deleting its volumes; its API results are excluded from acceptance.
- Latest visual change: Figma frame `280:10774` shows top bar title «Фильтры» and category chips in a horizontal wrapping row; source screenshot is available from Figma MCP. Android implementation was updated accordingly and rebuilt.
- PM verified `ANDROID_HOME=/private/tmp/srastemsya-sdk.7DCrTs GRADLE_USER_HOME=/private/tmp/srastemsya-gradle ./gradlew :androidApp:assembleDebug :shared:testAndroidHostTest --no-configuration-cache --console=plain` — BUILD SUCCESSFUL; current APK SHA-256 `1136b5319305782219c4a03e0a50bac780ed3bae9987780d4fe471ae18d06ee2`.
- Current Android test criteria remain subject to independent retest on the visually updated APK.
