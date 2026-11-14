# Acceptance — 13 Фильтры комнаты

Scope: screen 13 only. Reference: `docs/specs/swipe-ui/13-room-filters.md`, `docs/specs/swipe-ui/screens/280-10805.png`; shared rules: `docs/specs/swipe-ui/README.md`.

- AC-01: Existing frame shows «Фильтры», «Что ищем?», type, categories, «Бюджет на человека, ₽», event date, and «Запустить выбор»; active chips are blue.
- AC-02: The requested target behavior is per-participant filters, submit, wait for other participants, and only then route to 14/15. The supplied frame and interaction text explicitly lack the participant-specific copy, submit label and waiting design, while the backend contract does not support aggregation. This criterion is BLOCKED pending a target design and contract; do not infer or invent UI behavior.
- AC-03: Back returns to lobby 11/12 according to participant role, as supported by the public spec.

Do not claim PASS for the screen's complete target behavior while AC-02 remains unresolved. Capture the available frame state and report the documented blocker.
