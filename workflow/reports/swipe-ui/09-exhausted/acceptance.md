# Acceptance — 09 Больше вариантов нет

Scope: screen 09 only. Reference: `docs/specs/swipe-ui/09-exhausted.md`, `docs/specs/swipe-ui/screens/280-10943.png`; shared rules: `docs/specs/swipe-ui/README.md`.

- AC-01: Show «Лента», title «Больше вариантов нет», matching helper copy, white «Вы просмотрели всё» status card and blue «Изменить фильтры» button.
- AC-02: Empty state sits in upper screen area and has no card stack, skeleton or swipe actions.
- AC-03: Button returns to 03 or 13 for the current mode and preserves values; back follows current navigation.

Runtime transition verification depends on separately assigned swipe screens 04/05/14/15. Build now; do not claim PASS until those integrated flows are available.
