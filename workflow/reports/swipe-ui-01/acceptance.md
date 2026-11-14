# Acceptance — swipe-ui screen 01: Открытия

Scope: screen 01 visible state, actions originating on it, and the immediate transition required to verify those actions. Reference viewport is 390 × 844 px, accounting for platform status/safe insets separately.

## Immutable criteria

- **AC-01 — Initial visible state:** The Discovery tab is open and selected in the bottom tab bar. The top logo and bell, greeting “Куда срастёмся сегодня?”, two quick tiles, blue “Свайпать события” tile with its “Найди своё идеальное” caption, stacked-card illustration and circular arrow, and “Популярное рядом” heading with horizontal cards are visible in the screen composition. Compare hierarchy, text, wrapping, spacing, sizing and image crop with the reference PNG; dynamic card content need not reproduce the PNG's exact data. (Source: `docs/specs/swipe-ui/01-discovery.md`; `docs/specs/swipe-ui/screens/280-8319.png`; shared visual-check guidance in `docs/specs/swipe-ui/README.md`.)
- **AC-02 — Tile hit area and legibility:** Tapping any non-decorative part of the full “Свайпать события” tile activates it. Its decorative illustration does not obscure the caption or arrow. (Source: `docs/specs/swipe-ui/01-discovery.md`.)
- **AC-03 — Immediate transition:** Activating that tile navigates to screen 02, “Выбор устройства”. Verify only the arrival/state needed to establish this transition; do not assess screen 02's independent layout or behavior. (Source: `docs/specs/swipe-ui/01-discovery.md`; next-step identity/order in `docs/specs/swipe-ui/README.md`.)
- **AC-04 — Scroll and bottom navigation:** Vertically scrolling the Discovery feed does not move the bottom tab bar; the Discovery tab remains visibly selected. (Source: `docs/specs/swipe-ui/01-discovery.md`.)

## Scope exclusions and dependencies

No dependency is documented for screen 01. The tap's destination is screen 02 and is checked only as an immediate transition. No backend behavior is specified for this screen. No other screen is part of visual or regression acceptance.
