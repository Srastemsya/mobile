# FE-F02 Android UI regression rerun — 2026-11-13 14:45 UTC

- Platform: Android API 35 emulator `Srastemsya35`; iOS excluded.
- Revision: worktree based on `a67098fea02e445bdb3f64d85407fcf9a3b3933d` (the task packet's commit; working-tree correction is not represented by a new commit).
- APK: `/private/tmp/mobile-FE-F02/androidApp/build/outputs/apk/debug/androidApp-debug.apk`, built 2026-11-14 00:41:56 local; SHA-256 `918e2e614e6e25275d417377c9444733331edf01c14f00d4faa108531269bb23`. Installed successfully for this run.
- Commands/actions: `adb install -r .../androidApp-debug.apk`; `adb shell am start -n fefu.prime.srastem/fefu.prime.srastem.MainActivity`; ADB `input tap`, `input swipe`, `settings put system user_rotation`, `input keyevent 3`, `uiautomator dump`, `exec-out screencap` with resulting XML/text/screenshot inspection. All commands returned exit 0; emulator-side hierarchy pulls succeeded.
- Entry: app home → Feed → “На одном устройстве” → “Показать подборку”.

## Results

- AC-01: Single-device context and card stack visible; title “Выбор места”, context “Одно устройство · общий бюджет”, active card plus two decorative layers, counter/progress, action row and “Изменить фильтры” visible. Room context not assessed; room flow is not populated for this package.
- AC-02: Left swipe advanced from 1/20 to 2/20. Cross action advanced; Heart advanced. Directional animation was not established by endpoint screenshots and remains unverified.
- AC-03: Undo restored 1/20 and prior card after left swipe. After Undo, Heart made a different decision and advanced to 2/20. Undo was disabled on the first card in prior run; not retested here.
- AC-04: Counter/progress reflected observed positions 1/20 and 2/20. Pool-loading boundary / suppression was not reached or verified.
- AC-05: In portrait, observed placeholder cover art, title, category, “Цена неизвестна”, and “Владивосток”. The tested card did not expose a description. Full metadata completeness across available cards not established; tap behavior not tested.
- AC-06: After a SKIP at 1/20, rotated to landscape. The screen stayed on the swipe route at 2/20 with the same single-device context; Undo remained available and returned to 1/20. Landscape layout rendered an enlarged/cropped image and omitted card text from the accessibility hierarchy. Rotating back to portrait retained the restored 1/20 card. Then left swipe, Undo, and a different Heart decision succeeded.
- AC-06 background/return: At 2/20 after Heart, sent Home, brought the existing task to foreground, and observed the same active card at 2/20. Undo remained available and restored the prior card at 1/20. This tests background/foreground within the same process, not process death.

- Run counts: passed 2 criteria fully observed in the tested single-device scope (AC-03, AC-06); failed 0; partially verified 4 (AC-01 room scope, AC-02 animation, AC-04 loading boundary, AC-05 full metadata); skipped 0 standalone action scripts. Complete acceptance still has unverified mandatory subchecks, as detailed below.

## Verdict

AC-06 preservation is now **PASS** for rotation and background/foreground within the process. Core swipe, Cross, Heart, and Undo regression actions passed as observed. Full AC-01–AC-06 acceptance remains **BLOCKED / incomplete**: room mode, animation, pool-loading boundary, and full card metadata were not verified. No claim is made for skipped checks.

Evidence captures and public UI dumps are at `/private/tmp/fe-f02-rerun-landscape.png`, `/private/tmp/fe-f02-before-rotation.xml`, `/private/tmp/fe-f02-after-rotation-rerun.xml`, `/private/tmp/fe-f02-after-undo-rotation.xml`, `/private/tmp/fe-f02-after-background-rerun.xml`, and `/private/tmp/fe-f02-after-background-undo.xml`.
