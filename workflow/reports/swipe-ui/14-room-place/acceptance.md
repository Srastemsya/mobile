# Acceptance — 14 Карточка места · комната

Scope: screen 14 and its own gestures only. Reference: `docs/specs/swipe-ui/14-room-place.md`, `docs/specs/swipe-ui/screens/280-10889.png`; shared geometry/gesture rules: `docs/specs/swipe-ui/README.md`.

- AC-01: At 390 × 844 show «Выбор места» with filter, room title («Комната: Вечер пятницы» in the reference), participant avatars and a card stack with «Бар „Миллионка“». Preserve 04 card geometry, photo crop, safe areas; no buttons/counter/progress.
- AC-02: Avatar row does not overlap stack; filter is available only for allowed role, with at least 44 × 44 px hit area.
- AC-03: Both left and right card-body swipes follow the finger, record the room decision and reveal the next card; next card rises and exactly one active remains. Quick repeat does not duplicate/skip state.
- AC-04: Reverse/cancel a gesture and confirm no vote is recorded until completion. After final card open 16; if room result is ready, open 17.

Capture initial frame and screen recording covering left/right, quick repeat, cancel and transition evidence. Check only 14 and immediate destinations; do not visually audit those screens.
