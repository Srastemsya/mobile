# Acceptance — 16 Ожидание остальных

Scope: screen 16 only. Reference: `docs/specs/swipe-ui/16-room-wait.md`, `docs/specs/swipe-ui/screens/280-10964.png`; shared rules: `docs/specs/swipe-ui/README.md`.

- AC-01: Show «Лента», «Ваш голос принят», majority/new-round explanation, status card «Ждём участников» / «Голос отправлен», and «В начало ленты» button.
- AC-02: Waiting is static and offers no new swipes; status changes automatically to 17 when a result is ready.
- AC-03: A new round transitions to 07 and then the applicable swipe card; start button returns to mode selection.

Runtime transition verification depends on separately assigned room swipe screens 14/15 and matching backend readiness. Build now; do not claim PASS until the integrated flow is available.
