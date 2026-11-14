# Acceptance — 06 Выбор сделан

Scope: screen 06 only. Reference: `docs/specs/swipe-ui/06-selected.md`, `docs/specs/swipe-ui/screens/280-10985.png`; shared rules: `docs/specs/swipe-ui/README.md`.

- AC-01: At 390 × 844 show «Выбор сделан» top bar, full-width cover, blue «Место выбрано» chip for a place, title, attributes and description, plus a lower «К месту» action.
- AC-02: This is a full-screen result rather than a card stack; button clears system bottom inset; title/action match item type.
- AC-03: The item action opens the selected object's details; back follows app navigation.

Runtime transition verification depends on separately assigned swipe screen 04/05. Build now; do not claim PASS until the integrated swipe flow is available.
