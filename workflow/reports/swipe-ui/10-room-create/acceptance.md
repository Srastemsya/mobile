# Acceptance — 10 Создание комнаты

Scope: screen 10 only, on the current integrated Android candidate. Reference: `docs/specs/swipe-ui/10-room-create.md`, `docs/specs/swipe-ui/screens/280-10707.png`; shared geometry and inset guidance: `docs/specs/swipe-ui/README.md`.

- AC-01: At 390 × 844 px, render the screen with top bar «Новая комната», title «Создайте комнату», explanatory caption about joining friends, room-name field with example «Вечер пятницы», and blue «Создать комнату» button. Respect system insets.
- AC-02: The bottom tab bar is hidden; content sits in the upper portion of the screen; the field and button have equal widths.
- AC-03: Room name can be entered in the field; the placeholder/example is visible when empty.
- AC-04: Back returns to screen 02.
- AC-05: Submitting a valid room name proceeds to screen 11 (invitation); verify the transition without auditing screen 11's own visual acceptance.
- AC-06: Empty/invalid field feedback follows the UI Kit field error presentation.

Record screenshots/video at initial state and each action/transition. Verify only screen 10 and its immediate transitions; no room invitation onward flow assessment.
