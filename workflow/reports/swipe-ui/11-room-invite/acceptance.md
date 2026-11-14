# Acceptance — 11 Приглашение в комнату

Scope: screen 11 only. Reference: `docs/specs/swipe-ui/11-room-invite.md`, `docs/specs/swipe-ui/screens/280-10728.png`; shared rules: `docs/specs/swipe-ui/README.md`.

- AC-01: Show «Комната» top bar, room name, invite friends helper, participant avatars/count, invitation card with link icon, explanation and «Скопировать ссылку», then «К фильтрам».
- AC-02: Participant count/avatars fit the layout; a long room name does not overlap content; respect insets.
- AC-03: Copy link transitions to screen 12.
- AC-04: «К фильтрам» navigates to screen 13; back returns to the previous screen.
- AC-05: Any participant presentation/state not represented by the supplied frame is not judged visually; report missing design separately.

Check only screen 11 and immediate transitions. Capture initial state and action/transition evidence.
