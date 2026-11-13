# Rule routing
Read this index; load only rules relevant to the assigned role and task. Files here are explicit instructions, not Cursor glob/alwaysApply automation. New rules need a row with scope and roles; existing rules must be preserved and conflicts raised.

| File | Roles | Scope |
|---|---|---|
| testing.md | Developer, Tester | Test ownership and immutable evidence |
| [design.md](design.md) | Design, Developer | Figma sections, reusable components, color and gradient tokens, and frontend handoff documentation |
| [mobile-architecture.md](mobile-architecture.md) | Developer | KMP app layers (domain/data/presentation), dependency inversion, DI, networking, UI Kit usage |
| [feature-docs.md](feature-docs.md) | Developer | Mandatory FEATURE.md per mobile feature directory |

PM receives a short checklist of applicable obligations and evidence, not all implementation rules. Tester may read behavioral requirements from architecture rules, but must not inspect implementation to verify them. Developer runs source-level compliance checks.
