# FE-F01 Android runtime recheck

- Command: install `androidApp/build/outputs/apk/debug/androidApp-debug.apk` with `/private/tmp/srastemsya-sdk.7DCrTs/platform-tools/adb install -r`, start `fefu.prime.srastem/.MainActivity`, and manually exercise the public app UI on `emulator-5554`.
- Exit code: 0 for APK installation and activity launch; manual UI observations recorded below.
- Passed: 2 — date control accepted `14.11.2026`; EVENT with All/no date returned a nonempty list (20 visible event titles).
- Failed: 0.
- Skipped: 1 — EVENT category+date returned an empty list; Figma frame comparison unavailable in this runtime session.
- Build: APK timestamp 2026-11-13 23:38 local; SHA-256 `ee564849a2c51c47feeb7b0efb57691b4d8831398b70171394fbdeab491beb92`.
- Evidence: [screenshots](../evidence/FE-F01/20261113T234300Z-android/).
- Note: `SrDateField` displayed a native Android calendar picker; selecting Nov 14, 2026 populated `14.11.2026`.
- EVENT category “События для проверки свайпов” + 2026-11-14 submitted and showed “Подходящих вариантов пока нет. Измените фильтры.”
- EVENT with no date and category All submitted and displayed a full list of candidates, including “Вечер джаза у моря”, “Прогулка к маяку”, and “Выставка современного искусства”.
