# Acceptance — 12 Ссылка скопирована

Scope: screen 12 only. Reference: `docs/specs/swipe-ui/12-room-copied.md`, `docs/specs/swipe-ui/screens/280-10751.png`; shared rules: `docs/specs/swipe-ui/README.md`.

- AC-01: Preserve room header/details and show compact white confirmation card with checkmark, «Ссылка скопирована» and explanation.
- AC-02: Card changes content and height from screen 11; «К фильтрам» is visible below it. Confirmation is not a toast over the button.
- AC-03: Continue navigates to screen 13 for organizer; back follows current app behavior.
- AC-04: Recopy behavior and confirmation duration follow current app logic; do not assert an undocumented duration.

Check only screen 12 and immediate transitions. Capture initial state and action/transition evidence.
