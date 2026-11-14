# Acceptance — 04 Карточка места · одно устройство

Scope: screen 04 and its own gestures only. Reference: `docs/specs/swipe-ui/04-solo-place.md`, `docs/specs/swipe-ui/screens/280-10836.png`; shared geometry/gesture rules: `docs/specs/swipe-ui/README.md`.

- AC-01: At 390 × 844, show «Выбор места», back and filter controls, «Одно устройство · общий бюджет», two visible backing cards and one active white place card with large rounded, correctly cropped photo, title, chips and description. Respect system insets; no buttons, counter or progress bar.
- AC-02: Photo is not stretched; backing cards are visible above and beside active card; content stays legible in safe area.
- AC-03: Filter hit area is at least 44 × 44 px and opens 03; back follows current navigation.
- AC-04: A left swipe begun on the card body follows the finger and dismisses to the next card; the next card rises from the stack; after animation exactly one active card remains. Quick repeated swipe does not duplicate/skip active state.
- AC-05: A right swipe selects the item and opens 06. Verify reversal/cancel behavior for the current gesture, then confirm selection only after completed threshold-crossing gesture.
- AC-06: Skipping the final card transitions to 07.

Capture initial frame and screen recording covering both gesture directions, quick repeat, cancellation and immediate transitions. Check only 04 and immediate destinations; do not visually audit their screens.
