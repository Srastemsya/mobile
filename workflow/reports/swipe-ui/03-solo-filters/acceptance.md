# Acceptance — 03 Общие фильтры

Scope: screen 03 only. Reference: `docs/specs/swipe-ui/03-solo-filters.md`, `docs/specs/swipe-ui/screens/280-10774.png`; shared rules: `docs/specs/swipe-ui/README.md`.

- AC-01: At 390 × 844 px show top bar «Фильтры» with back arrow, «Что ищем?», single-device helper text, groups «Тип подборки» and «Категории», «Общий бюджет, ₽» field, event date field, and blue «Начать выбор» button.
- AC-02: Selected chips are blue, unselected chips light; fields and button align to the common grid; choosing «Места» hides date.
- AC-03: Entry from screen 02 after «Свайпать события» has «События» selected.
- AC-04: Back returns to screen 02; starting selection routes to 05 for events or 04 for places.
- AC-05: Returning from the immediate card screen restores the chosen filters.

Check only this screen and immediate transitions. Capture initial state, changes, and transition screenshots.
