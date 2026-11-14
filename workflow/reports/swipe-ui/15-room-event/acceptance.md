# Acceptance — 15 Карточка события · комната

Scope: screen 15 and its own gestures only. Reference: `docs/specs/swipe-ui/15-room-event.md`, `docs/specs/swipe-ui/screens/280-10916.png`; shared geometry/gesture rules: `docs/specs/swipe-ui/README.md`.

- AC-01: At 390 × 844 show «Выбор события» with filter, room title/avatars, stack and event «Сапы на Тобизина» with photo, chips and description. Preserve 14 stack geometry; dynamic text must not clip; no buttons/counter/progress.
- AC-02: Avatar row does not overlap stack; filter is available only for allowed role, with at least 44 × 44 px hit area.
- AC-03: Left and right card-body swipes follow the finger, record a decision and reveal next candidate; next card rises and exactly one active remains. Quick repeat does not duplicate/skip state.
- AC-04: Reverse/cancel a gesture and confirm it restores the same card position and records no vote. After final card open 16; if room result is ready, open 17.

Capture initial frame and screen recording covering left/right, quick repeat, cancel and transition evidence. Check only 15 and immediate destinations; do not visually audit those screens.
