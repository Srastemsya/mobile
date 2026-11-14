# Specification queue
Act as a compact dispatcher. Read `workflow/config.json` and `workflow/reports/progress.md`; require an unambiguous spec directory and base branch. Inventory specs using paths and short summaries; record dependencies only from documented requirements. Do not invent completion.

Only PM and Developer are active roles. Process the numbered `docs/specs/swipe-ui/01`–`17` screens in order and use the README for scenario dependencies. PM writes scoped, immutable acceptance criteria for the current screen and resolves documented gaps. Developer implements that screen, updates feature/design documentation, runs applicable developer checks and records exact results. PM reviews the implementation diff, documentation and Developer report before advancing. No Tester handoff or separate testing role is used; unverified runtime behavior must be reported as unverified.

Use one screen at a time on `new_ui`. Keep changes scoped, preserve pre-existing edits, and do not create isolated worktrees unless explicitly needed. Do not push, create a PR, deploy, or merge into another branch unless the user asks. Do not invent backend behavior: the per-member room-filter aggregation on screen 13 remains blocked by the documented backend contract gap.

Save progress after each screen transition. Report current status, dependencies, blockers, changed files, check results and the exact revision. A passing build or source check is not evidence of device acceptance.
