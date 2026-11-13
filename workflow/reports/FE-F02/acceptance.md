# FE-F02 acceptance criteria

Scope: Android swipe screen and card stack; iOS is explicitly excluded. Sources: `workflow/specs/feed-v1/FE-F02.md`, `docs/specs/feed-v1/implementation.md` § FE-F02, `docs/specs/feed-v1/acceptance.md` F02-01, `docs/design/feed.md`, `docs/design/ux-personas-scenarios.md`. Backend behavior is out of scope.

- AC-01 — Both single-device and room contexts show the correct title/context and a card stack with active card, decorative stack layers, position/progress, filter navigation and vote actions, using available UI Kit components and tokens. Align reasonably with Figma frames 280-10836, 280-10862 and 280-10889 / Page 2 280-7618.
- AC-02 — Left swipe and Skip/Cross produce SKIP; right swipe and Like/Heart produce LIKE. A decision advances to the next card with directional transition/animation.
- AC-03 — Undo is unavailable at the first card; after a decision it restores the preceding card and clears that decision so a different choice can be made. Current-pool decisions are mutable until pool completion.
- AC-04 — Position counter and progress reflect the current card position and pool size; loading the next pool suppresses swipe actions (boundary integration only; pool fetching itself belongs to FE-F03/FE-F05).
- AC-05 — Card presents available cover with project placeholder when absent, title, category, price/price tier, address/date and description as applicable. Card tap behavior remains unspecified by source and must not be invented as navigation.
- AC-06 — Rotation/backgrounding preserves current card, decisions, and undo ability within the process/ViewModel scope.
- AC-07 — Architecture and feature documentation obligations are met: presentation state/event/ViewModel organization and domain boundary are respected, no iOS implementation, feature `FEATURE.md` is current, and DI/docs are updated if bindings change. Developer supplies source-level evidence; Tester does not inspect source.
- AC-08 — Relevant Android build and unit/host tests pass; independent black-box UI acceptance verifies AC-01–AC-06 on the identified revision, or documents a concrete infrastructure blocker. No PASS on skipped mandatory checks.

## Clarification from product owner (2026-11-14)

- AC-09 — A visible feed pool contains at most 10 cards (target is 10 when enough candidates exist), not 20. The current public API has no page-size/limit query parameter and may return up to 20 per cursor page; the client must preserve fetched overflow for the next local 10-card pool rather than silently drop it. Verify that position/progress reset at the 10-card pool boundary and cards are not duplicated or skipped across the local split. Do not claim server page-size behavior changed.
