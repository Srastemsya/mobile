# Acceptance — 07 Обновление подборки

Scope: screen 07 only. Reference: `docs/specs/swipe-ui/07-refresh.md`, `docs/specs/swipe-ui/screens/280-11049.png`; shared rules: `docs/specs/swipe-ui/README.md`.

- AC-01: Show «Лента» top bar, centered vertical card skeleton with photo placeholder, text lines and animated blue spinner, followed by «Обновляем подборку» and the filter explanation.
- AC-02: No old active card or card actions are visible; no swipes/actions are available during loading; skeleton geometry remains stable.
- AC-03: Successful loading routes to 08; empty result routes to 09.

Runtime transition verification depends on separately assigned swipe screens 04/05. Build now; do not claim PASS until the integrated swipe flow is available.
