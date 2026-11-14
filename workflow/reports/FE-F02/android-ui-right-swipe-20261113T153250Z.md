# FE-F02 right-swipe UI follow-up — 2026-11-13 15:32 UTC

- Platform: Android API 35 emulator `emulator-5554` (`Srastemsya35`); iOS excluded.
- APK SHA-256: `8f33de281fe2da0256f499361fd6899cd8d03600835611fa15a1caf76b97a71c` (same as final rerun APK).
- Before: screenshot shows active “Стендовый вариант 5” at `1 / 10`, with metadata “Места для проверки свайпов · 10 ₽ · Владивосток”.
- Action: one horizontal rightward gesture across the card body, ADB coordinates `(250,1000) → (800,1000)`, 700 ms. The gesture was performed in the card region and did not touch the separate lower Heart control row.
- After: screenshot shows active “Стендовый вариант 6” at `2 / 10`, with the same available metadata. The gesture therefore advances independently of tapping the Heart button; no Heart button was tapped in this narrow follow-up.
- Result: PASS for rightward swipe action → next card/position. A right-swipe animation frame was not captured in this follow-up.
- Artifacts: `/private/tmp/fe-f02-right-before.png`, `/private/tmp/fe-f02-right-before.xml`, `/private/tmp/fe-f02-right-after.png`, `/private/tmp/fe-f02-right-after.xml`.
