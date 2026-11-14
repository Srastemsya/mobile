# Acceptance — 17 Матч найден

Scope: screen 17 only. Reference: `docs/specs/swipe-ui/17-room-match.md`, `docs/specs/swipe-ui/screens/280-11004.png`; shared rules: `docs/specs/swipe-ui/README.md`.

- AC-01: Show «Матч найден», full-width photo, blue «Выбор большинства» chip, title, attributes, description and lower action «К месту» / «К мероприятию».
- AC-02: Full-screen composition follows 06, with room-result title and label; participants see the same selected item.
- AC-03: Action opens selected-object details; back follows room navigation.

Runtime transition verification depends on separately assigned room swipe screens 14/15 and a server room result. Build now; do not claim PASS until the integrated flow is available.
