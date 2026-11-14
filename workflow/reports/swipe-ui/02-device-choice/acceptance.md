# Acceptance — 02 Выбор устройства

Scope: screen 02 only, on the current integrated Android candidate. Reference: `docs/specs/swipe-ui/02-device-choice.md`, `docs/specs/swipe-ui/screens/280-10679.png`; shared geometry and inset guidance: `docs/specs/swipe-ui/README.md`.

- AC-01: At 390 × 844 px, render the 02 screen matching the reference hierarchy and content: top bar «Лента», title «Выбираем вместе», explanatory text, and the two device-choice cards with their icons and two-line descriptions. Respect system insets; no bottom tab bar.
- AC-02: The two choice cards have equal width, readable descriptions, and full-card tappable areas.
- AC-03: Back returns to «Открытия» (screen 01).
- AC-04: Tapping «На одном устройстве» opens screen 03.
- AC-05: Tapping «На нескольких устройствах» opens screen 10.
- AC-06: Screen 10 starts with the previously selected «События» mode carried forward from the entry tile, as required by the shared scenario.

Record screenshots/video at initial state and each action/transition. Verify only screen 02 and its immediate transitions; do not assess visual quality of destination screens beyond confirming navigation and inherited mode.
