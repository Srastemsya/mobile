# Acceptance — 05 Карточка события · одно устройство

Scope: screen 05 and its own gestures only. Reference: `docs/specs/swipe-ui/05-solo-event.md`, `docs/specs/swipe-ui/screens/280-10862.png`; shared geometry/gesture rules: `docs/specs/swipe-ui/README.md`.

- AC-01: At 390 × 844, show «Выбор события», back and filter controls, single-device summary, two backing cards and one active white event card with photo, title, category/price/place chips and short description. Respect safe areas; no buttons, counter or progress bar.
- AC-02: Header/chips do not overlap photo or description; card stays within safe area; filter hit area is at least 44 × 44 px.
- AC-03: A left swipe begun on the card body follows the finger and moves to the next event or 07; the next card rises from the stack and exactly one active card remains. Quick repeated swipe does not duplicate/skip active state.
- AC-04: A right swipe opens 06 with the selected event. Cancel/reverse a gesture and verify it restores the same card position; selection occurs only on a completed gesture.
- AC-05: Filter opens 03; back follows current navigation.

Capture initial frame and screen recording covering both directions, quick repeat, cancellation and immediate transitions. Check only 05 and immediate destinations; do not visually audit their screens.
