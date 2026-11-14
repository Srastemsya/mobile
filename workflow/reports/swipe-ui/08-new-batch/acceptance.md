# Acceptance — 08 Первая карточка новой подборки

Scope: screen 08 only. Reference: `docs/specs/swipe-ui/08-new-batch.md`, `docs/specs/swipe-ui/screens/280-11023.png`; shared rules: `docs/specs/swipe-ui/README.md`.

- AC-01: Show «Выбор места» top bar, mode row and stack with active sample «Кофейня с видом», image, chips and description; mode-dependent header follows current selection.
- AC-02: Loading skeleton is replaced by the stack without a blank frame; card geometry matches 04/05 and photo is not stretched.
- AC-03: Subsequent actions use the same gesture flow and transitions as 04/05.

Runtime transition verification depends on separately assigned swipe screens 04/05. Build now; do not claim PASS until the integrated swipe flow is available.
