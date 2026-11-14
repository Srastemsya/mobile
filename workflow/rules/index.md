# Rule routing
Read this index; load only rules relevant to the assigned role and task. Files here are explicit instructions, not Cursor glob/alwaysApply automation. New rules need a row with scope and roles; preserve existing rules and raise conflicts.

| File | Roles | Scope |
|---|---|---|
| testing.md | Developer, PM | Developer checks and immutable evidence; PM reviews reported evidence |
| [design.md](design.md) | PM, Developer | Figma sections, reusable components, color and gradient tokens, and frontend handoff documentation |
| [mobile-architecture.md](mobile-architecture.md) | Developer | KMP app layers (domain/data/presentation), dependency inversion, DI, networking, UI Kit usage |
| [feature-docs.md](feature-docs.md) | Developer, PM | Mandatory FEATURE.md per mobile feature directory and PM review |

PM receives a short checklist of applicable obligations and evidence. Developer runs source-level compliance checks and reports exact results; PM reviews the report, documentation and implementation diff.
